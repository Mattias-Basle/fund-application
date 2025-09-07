package com.example.fund_app.batch;

import com.example.fund_app.feign.ERApiResponse;
import com.example.fund_app.feign.ExchangeRateClient;
import com.example.fund_app.model.Currency;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;


@Disabled
@SpringBatchTest
@SpringJUnitConfig(classes = {ExchangeRateBatchConfiguration.class, BatchTestConfig.class})
public class ExchangeRateBatchTest {

    @MockitoBean
    private ExchangeRateClient exchangeRateClient;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @AfterEach
    public void cleanUp() {
        jobRepositoryTestUtils.removeJobExecutions();
    }


    @Test
    void test(@Autowired Job job) throws Exception {
        // Given
        this.jobLauncherTestUtils.setJob(job);
        ERApiResponse response = new ERApiResponse("success", "USD", getRates());

        // When
        doReturn(ResponseEntity.ofNullable(response)).when(exchangeRateClient).fetchRatesPerCurrency(Currency.USD);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(new JobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        // Then
        assertEquals("exchangeRateJob", actualJobInstance.getJobName());
        assertEquals(ExitStatus.COMPLETED, actualJobExitStatus);
    }

    private Map<String, BigDecimal> getRates() {
        Map<String, BigDecimal> map = new HashMap<>();
        map.put("EUR", new BigDecimal("0.9874367"));
        map.put("BRL", new BigDecimal("5.4674327"));
        map.put("GBP", new BigDecimal("0.8465376"));
        return map;
    }


}
