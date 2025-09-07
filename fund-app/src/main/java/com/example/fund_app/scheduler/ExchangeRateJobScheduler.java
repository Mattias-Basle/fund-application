package com.example.fund_app.scheduler;

import com.example.fund_app.exception.ScheduledJobRunningException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class ExchangeRateJobScheduler {

    private final Job job;
    private final JobLauncher jobLauncher;

    public ExchangeRateJobScheduler(Job job, JobLauncher jobLauncher) {
        this.job = job;
        this.jobLauncher = jobLauncher;
    }

    @Scheduled(cron = "${spring.batch.cron.expression.xrate}")
    public void runExchangeRateBatch() {
        try {
            log.info("[SCHEDULER] Starting job to update exchanges rates");
            JobExecution jobExecution = jobLauncher.run(job, buildParameters());

            if (ExitStatus.COMPLETED.equals(jobExecution.getExitStatus())) {
                log.info("[SCHEDULER] Exchanges rates updated");
            } else {
                log.info("[SCHEDULER] Exchange rates not updated properly, job status: {}", jobExecution.getExitStatus());
            }

        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            log.error("[SCHEDULER] Error during the batch job: {}", e.getMessage());
            throw new ScheduledJobRunningException(e.getMessage());
        }

    }

    private JobParameters buildParameters() {
        return new JobParametersBuilder()
                .addLocalDateTime("timestamp", LocalDateTime.now())
                .toJobParameters();
    }

}
