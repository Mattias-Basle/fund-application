package com.example.fund_app.mapper;

import com.example.fund_app.model.Account;
import com.example.fund_app.model.Owner;
import com.example.fund_app.model.dto.AccountDetailsViewDto;
import com.example.fund_app.model.dto.OwnerDetailsViewDto;
import com.example.fund_app.model.dto.OwnerViewDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.data.domain.Page;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OwnerMapper {

    @Mapping(target = "accountIds", source = "accounts")
    OwnerViewDto toDto(Owner owner);

    default Page<OwnerViewDto> toDto(Page<Owner> owners) {
        return owners.map(
                owner -> {
                    Set<Long> accountIds = mapAccounts(owner.getAccounts());
                    return new OwnerViewDto(owner.getId(), owner.getUsername(), accountIds);
                });
    };

    @Mapping(target = "accountDetails", source = "accounts")
    OwnerDetailsViewDto toDetailsDto(Owner owner);

    default Set<Long> mapAccounts(Set<Account> accounts) {
        return accounts.stream().map(Account::getAccountId).collect(Collectors.toSet());
    }

    default Set<AccountDetailsViewDto> mapAccountsDetails(Set<Account> accounts) {
        return accounts.stream().map(account ->
                new AccountDetailsViewDto(account.getAccountId(), account.getCurrency(), account.getBalance()))
                .collect(Collectors.toSet());
    }
}
