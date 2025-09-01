package com.example.fund_app.batch;

import com.example.fund_app.feign.ExchangeRateClient;
import com.example.fund_app.mapper.ExchangeRateMapper;
import com.example.fund_app.model.ExchangeRate;
import com.example.fund_app.repository.ExchangeRateRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ConditionalOnBean(value = {ExchangeRateClient.class, ExchangeRateRepository.class, ExchangeRateMapper.class})
public class ExchangeRateBatchConfiguration {


    @Bean
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
    public ItemReader<ExchangeRate> reader(ExchangeRateRepository repository, Pageable pageable) {
        return new RepositoryItemReaderBuilder<ExchangeRate>()
                .name("exchangeRateItemReader")
                .repository(repository)
                .methodName("findAll")
                .arguments(pageable)
                .build();
    }

    @Bean
    public ItemProcessor<ExchangeRate, ExchangeRate> processor(ExchangeRateClient client, ExchangeRateMapper mapper) {
        return new ExchangeRateItemProcessor(client, mapper);
    }

    @Bean
    public ItemWriter<ExchangeRate> writer(ExchangeRateRepository repository) {
        return new ExchangeRateItemWriter(repository);
    }
}
