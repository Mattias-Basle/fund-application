package com.example.fund_app.feign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ERApiResponse(
    String result,
    @JsonProperty(value = "base_code")
    String baseCode,
    Map<String, BigDecimal> rates
) {
}
