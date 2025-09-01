package com.example.fund_app.repository;

import com.example.fund_app.model.Currency;
import com.example.fund_app.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Currency> {
}
