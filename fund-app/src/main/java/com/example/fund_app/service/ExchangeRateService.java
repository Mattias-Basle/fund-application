package com.example.fund_app.service;

import com.example.fund_app.exception.ExchangeRateNotRetrievableException;
import com.example.fund_app.feign.ERApiResponse;
import com.example.fund_app.feign.ExchangeRateClient;
import com.example.fund_app.mapper.ExchangeRateMapper;
import com.example.fund_app.model.Currency;
import com.example.fund_app.model.ExchangeRate;
import com.example.fund_app.repository.ExchangeRateRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    @Qualifier("xRateCacheManager")
    private final CacheManager exchangeRateCacheManager;
    private final ExchangeRateClient exchangeRateClient;
    private final ExchangeRateMapper exchangeRateMapper;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository, CacheManager exchangeRateCacheManager, ExchangeRateClient exchangeRateClient, ExchangeRateMapper exchangeRateMapper) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateCacheManager = exchangeRateCacheManager;
        this.exchangeRateClient = exchangeRateClient;
        this.exchangeRateMapper = exchangeRateMapper;
    }

    public BigDecimal getRate(Currency in, Currency out) {
        Cache cache = exchangeRateCacheManager.getCache("XRATE_CACHE");
        ExchangeRate xRate = cache.get(in, ExchangeRate.class);

        if (xRate == null) {
            xRate = exchangeRateRepository.findById(in)
                    .orElse(retrieveExchangeRate(in));
        }

        return xRate.getRates().get(out);
    }

    private ExchangeRate retrieveExchangeRate(Currency in) {
        ExchangeRate retrievedRate = exchangeRateMapper.toEntity(
                findRatesByCurrency(in), Instant.now());

        ExchangeRate savedRate =  exchangeRateRepository.save(retrievedRate);
        Cache cache = exchangeRateCacheManager.getCache("XRATE_CACHE");
        cache.put(in, savedRate);
        return savedRate;
    }

    private ERApiResponse findRatesByCurrency(Currency currency) {
        ERApiResponse response =  exchangeRateClient.fetchRatesPerCurrency(currency).getBody();
        if (!"success".equalsIgnoreCase(response.result())) {
            throw new ExchangeRateNotRetrievableException("Could not retrieve the exchange rate for " + currency);
        }
        return response;
    }
}
