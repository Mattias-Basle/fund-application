package com.example.fund_app.model;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@Embeddable
public class Amount {

    private Currency currency;
    private BigDecimal value;
}
