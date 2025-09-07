package com.example.fund_app.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Amount {

    private Currency currency;
    private BigDecimal value;
}
