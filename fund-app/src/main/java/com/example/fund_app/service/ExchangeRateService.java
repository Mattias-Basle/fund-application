package com.example.fund_app.service;

import com.example.fund_app.exception.ExchangeRateNotRetrievableException;
import com.example.fund_app.feign.ERApiResponse;
import com.example.fund_app.feign.ExchangeRateClient;
import com.example.fund_app.mapper.ExchangeRateMapper;
import com.example.fund_app.model.Currency;
import com.example.fund_app.model.ExchangeRate;
import com.example.fund_app.repository.ExchangeRateRepository;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateClient exchangeRateClient;
    private final ExchangeRateMapper exchangeRateMapper;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository,
                               ExchangeRateClient exchangeRateClient,
                               ExchangeRateMapper exchangeRateMapper) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateClient = exchangeRateClient;
        this.exchangeRateMapper = exchangeRateMapper;
    }

    public BigDecimal getRate(Currency in, Currency out) {
        ExchangeRate xRate = exchangeRateRepository.findByCurrencyAndLastUpdatedAt(in, LocalDate.now())
                    .orElseGet(() -> retrieveExchangeRate(in));

        return xRate.getRates().get(out);
    }

    private ExchangeRate retrieveExchangeRate(Currency in) {
        ExchangeRate retrievedRate = exchangeRateMapper.toEntity(
                findRatesByCurrency(in), LocalDate.now());

        ExchangeRate savedRate =  exchangeRateRepository.save(retrievedRate);
        return savedRate;
    }

    private ERApiResponse findRatesByCurrency(Currency currency) {
        ResponseEntity<ERApiResponse> response =  exchangeRateClient.fetchRatesPerCurrency(currency);
        if (!response.getStatusCode().equals(HttpStatusCode.valueOf(200))) {
            throw new ExchangeRateNotRetrievableException("Could not retrieve the exchange rate for " + currency);
        }

        ERApiResponse responseBody = response.getBody();
        if (!"success".equalsIgnoreCase(responseBody.result())) {
            throw new ExchangeRateNotRetrievableException("Could not retrieve the exchange rate for " + currency);
        }
        return responseBody;
    }
}
