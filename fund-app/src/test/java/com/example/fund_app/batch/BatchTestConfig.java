package com.example.fund_app.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
@EnableJpaRepositories(basePackages = {"com.example.fund_app.repository"})
@EntityScan(basePackages = {"com.example.fund_app.model"})
@ComponentScan(basePackages = {"com.example.fund_app.mapper"})
public class BatchTestConfig {

//    @Bean
//    public DataSource dataSource() {
//        return new EmbeddedDatabaseBuilder()
//                .setType(EmbeddedDatabaseType.H2)
//                .addScript("/org/springframework/batch/core/schema-h2.sql")
//                .addScript("classpath:/init.sql")
//                .build();
//    }
//
//    @Bean
//    public PlatformTransactionManager transactionManager() {
//        return new DataSourceTransactionManager(dataSource());
//    }
//
//    @Bean("entityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
//        var localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
//        localContainerEntityManagerFactoryBean.setDataSource(dataSource());
//        localContainerEntityManagerFactoryBean.setPackagesToScan("com.example.fund_app");
//        var hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
//        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
//        return localContainerEntityManagerFactoryBean;
//
//
//    }
//
//    @Bean
//    public JobRepository jobRepository() throws Exception {
//        var jobrepositoryFactoryBean = new JobRepositoryFactoryBean();
//        jobrepositoryFactoryBean.setDataSource(dataSource());
//        jobrepositoryFactoryBean.setTransactionManager(transactionManager());
//        jobrepositoryFactoryBean.afterPropertiesSet();
//        return jobrepositoryFactoryBean.getObject();
//
//    }
}
