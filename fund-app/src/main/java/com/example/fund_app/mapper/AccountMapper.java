package com.example.fund_app.mapper;

import com.example.fund_app.model.Account;
import com.example.fund_app.model.Owner;
import com.example.fund_app.model.dbo.AccountDbo;
import com.example.fund_app.model.dbo.OwnerDbo;
import com.example.fund_app.model.dto.AccountViewDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    AccountViewDto toDto(Account account);

    Account toModel(AccountDbo dbo);

    AccountDbo toDbo(Account model);

    default Owner mapOwnerDbo(OwnerDbo ownerDbo) {
        return Owner.builder()
                .id(ownerDbo.getId())
                .username(ownerDbo.getUsername())
                .accounts(null)
                .version(ownerDbo.getVersion())
                .build();
    }
}
