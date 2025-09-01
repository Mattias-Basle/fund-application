package com.example.fund_app.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "exchange_rates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {

    @Id @Column(name = "CURRENCY")
    @Enumerated(value = EnumType.STRING)
    private Currency currency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "RATES")
    private Map<Currency, BigDecimal> rates;

    @Column(name = "LAST_UPDATED_AT", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Instant lastUpdatedAt;
}
