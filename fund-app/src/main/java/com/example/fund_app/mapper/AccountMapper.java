package com.example.fund_app.mapper;

import com.example.fund_app.model.Account;
import com.example.fund_app.model.dto.AccountViewDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    AccountViewDto toDto(Account account);
}
