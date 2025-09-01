package com.example.fund_app.mapper;

import com.example.fund_app.model.Account;
import com.example.fund_app.model.Owner;
import com.example.fund_app.model.dto.OwnerViewDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OwnerMapper {

    @Mapping(target = "accountIds", source = "accounts")
    OwnerViewDto toDto(Owner owner);

    List<OwnerViewDto> toDto(List<Owner> owners);

    default Set<Long> mapAccounts(Set<Account> accounts) {
        return accounts.stream().map(Account::getId).collect(Collectors.toSet());
    }
}
