package com.example.fund_app.batch;

import com.example.fund_app.feign.ExchangeRateClient;
import com.example.fund_app.mapper.ExchangeRateMapper;
import com.example.fund_app.model.ExchangeRate;
import com.example.fund_app.repository.ExchangeRateRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
public class ExchangeRateBatchConfiguration {

    @Bean
    @Primary
    public Job exchangeRateJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("exchangeRateJob", jobRepository).start(step).build();
    }

    @Bean
    public Step exchangeRateStep(JobRepository jobRepository,
                     PlatformTransactionManager transactionManager,
                     ItemReader<ExchangeRate> reader,
                     ItemProcessor<ExchangeRate, ExchangeRate> processor,
                     ItemWriter<ExchangeRate> writer) {
        return new StepBuilder("exchangeRateStep", jobRepository).
                <ExchangeRate, ExchangeRate> chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public ItemReader<ExchangeRate> reader(ExchangeRateRepository repository) {
        return new RepositoryItemReaderBuilder<ExchangeRate>()
                .name("exchangeRateItemReader")
                .repository(repository)
                .methodName("findAll")
                .sorts(Map.of("currency", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<ExchangeRate, ExchangeRate> processor(ExchangeRateClient client, ExchangeRateMapper mapper) {
        return new ExchangeRateItemProcessor(client, mapper);
    }

    @Bean
    public ItemWriter<ExchangeRate> writer(ExchangeRateRepository repository) {
        return new RepositoryItemWriterBuilder<ExchangeRate>()
                .repository(repository)
                .methodName("save")
                .build();
    }
}
