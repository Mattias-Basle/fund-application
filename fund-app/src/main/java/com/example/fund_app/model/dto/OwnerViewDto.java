package com.example.fund_app.model.dto;

import java.util.Set;

public record OwnerViewDto(
        Long id,
        String username,
        Set<Long> accountIds
) {
}
