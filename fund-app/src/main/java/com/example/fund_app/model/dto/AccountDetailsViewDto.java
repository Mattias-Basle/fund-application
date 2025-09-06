package com.example.fund_app.model.dto;

import com.example.fund_app.model.Currency;

import java.math.BigDecimal;

public record AccountDetailsViewDto(
        Long accountId,
        Currency currency,
        BigDecimal balance
) {}
