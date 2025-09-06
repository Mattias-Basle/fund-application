package com.example.fund_app.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Amount {

    private Currency currency;
    private BigDecimal value;
}
