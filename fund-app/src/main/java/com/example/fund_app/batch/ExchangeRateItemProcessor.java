package com.example.fund_app.batch;

import com.example.fund_app.feign.ERApiResponse;
import com.example.fund_app.feign.ExchangeRateClient;
import com.example.fund_app.model.Currency;
import com.example.fund_app.model.ExchangeRate;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class ExchangeRateItemProcessor implements ItemProcessor<ExchangeRate, ExchangeRate> {

    private final ExchangeRateClient exchangeRateClient;

    public ExchangeRateItemProcessor(ExchangeRateClient exchangeRateClient) {
        this.exchangeRateClient = exchangeRateClient;
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

        item.setRates(mapRates(response.rates()));

        return item;
    }

    private Map<Currency, BigDecimal> mapRates(Map<String, BigDecimal> response) {
        Map<Currency, BigDecimal> rates = new HashMap<>();
        response.forEach((k, v) -> rates.put(Currency.valueOf(k), v));
        return rates;
    }
}
