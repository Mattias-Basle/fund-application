package com.example.fund_app.mapper;

import com.example.fund_app.feign.ERApiResponse;
import com.example.fund_app.model.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.time.Instant;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExchangeRateMapper {


    @Mapping(target = "currency", source = "response.baseCode")
    @Mapping(target = "lastUpdatedAt", source = "timestamp")
    ExchangeRate toEntity(ERApiResponse response, Instant timestamp);
}
