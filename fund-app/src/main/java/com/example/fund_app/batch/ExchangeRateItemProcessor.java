package com.example.fund_app.batch;

import com.example.fund_app.feign.ERApiResponse;
import com.example.fund_app.feign.ExchangeRateClient;
import com.example.fund_app.mapper.ExchangeRateMapper;
import com.example.fund_app.model.ExchangeRate;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ExchangeRateItemProcessor implements ItemProcessor<ExchangeRate, ExchangeRate> {

    private final ExchangeRateClient exchangeRateClient;
    private final ExchangeRateMapper exchangeRateMapper;

    public ExchangeRateItemProcessor(ExchangeRateClient exchangeRateClient, ExchangeRateMapper exchangeRateMapper) {
        this.exchangeRateClient = exchangeRateClient;
        this.exchangeRateMapper = exchangeRateMapper;
    }

    @Override
    public ExchangeRate process(ExchangeRate item) throws Exception {
        if (item == null) {
            return item;
        }

        ERApiResponse response = exchangeRateClient.fetchRatesPerCurrency(item.getCurrency()).getBody();

        if (!"success".equalsIgnoreCase(response.result())) {
            return null;
        }

        return exchangeRateMapper.toEntity(response, Instant.now());
    }
}
