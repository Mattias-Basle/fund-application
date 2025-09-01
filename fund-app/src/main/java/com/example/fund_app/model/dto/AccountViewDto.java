package com.example.fund_app.model.dto;

import com.example.fund_app.model.Currency;

import java.math.BigDecimal;

public record AccountViewDto(
        Long ownerId,
        Currency currency,
        BigDecimal balance
) {
}
