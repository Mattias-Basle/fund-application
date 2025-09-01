package com.example.fund_app.batch;

import com.example.fund_app.model.ExchangeRate;
import com.example.fund_app.repository.ExchangeRateRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class ExchangeRateItemWriter implements ItemWriter<ExchangeRate> {

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateItemWriter(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Override
    public void write(Chunk<? extends ExchangeRate> chunk) throws Exception {
        exchangeRateRepository.saveAll(chunk);
    }
}
