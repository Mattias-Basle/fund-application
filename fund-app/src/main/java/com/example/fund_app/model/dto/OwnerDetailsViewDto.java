package com.example.fund_app.model.dto;

import java.util.Set;

public record OwnerDetailsViewDto(
        Long id,
        String username,
        Set<AccountDetailsViewDto> accountDetails
) {
}
