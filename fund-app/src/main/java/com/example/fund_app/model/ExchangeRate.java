package com.example.fund_app.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "exchange_rates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
public class ExchangeRate {

    @Id @Column(name = "CURRENCY")
    @Enumerated(value = EnumType.STRING)
    private Currency currency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "RATES")
    private Map<Currency, BigDecimal> rates;

    @Column(name = "LAST_UPDATED_AT", nullable = false)
    private LocalDate lastUpdatedAt;

    @Version
    @Column(name = "XRATE_LOCK_VERSION", nullable = false)
    @ColumnDefault("0")
    private Long version;
}
