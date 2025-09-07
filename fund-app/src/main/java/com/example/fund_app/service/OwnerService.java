package com.example.fund_app.service;

import com.example.fund_app.exception.DbRecordNotFoundException;
import com.example.fund_app.exception.OwnerActionInvalidException;
import com.example.fund_app.exception.OwnerAlreadyExistsException;
import com.example.fund_app.mapper.AccountMapper;
import com.example.fund_app.mapper.OwnerMapper;
import com.example.fund_app.model.Account;
import com.example.fund_app.model.Currency;
import com.example.fund_app.model.Owner;
import com.example.fund_app.repository.AccountRepository;
import com.example.fund_app.repository.OwnerRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;

@Service
@Transactional
public class OwnerService {

    private final OwnerRepository ownerRepository;

    private final OwnerMapper ownerMapper;
    private final AccountRepository accountRepository;

    private final AccountMapper accountMapper;

    public OwnerService(OwnerRepository ownerRepository, OwnerMapper ownerMapper, AccountRepository accountRepository, AccountMapper accountMapper) {
        this.ownerRepository = ownerRepository;
        this.ownerMapper = ownerMapper;
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    @CachePut(value = "ownersCache", key = "#result.id")
    public Owner createOwner(String username) {
        if (ownerRepository.existsByUsername(username)) {
            throw new OwnerAlreadyExistsException(username + " already exists");
        }

        Owner newOwner = Owner.builder()
                .username(username)
                .accounts(new HashSet<>())
                .build();

        return ownerMapper.toModel(ownerRepository.save(ownerMapper.toDbo(newOwner)));
    }

    public Page<Owner> getAllOwners(Pageable pageable) {
        return ownerMapper.toModel(ownerRepository.findAll(pageable));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "ownersCache", key = "#ownerId")
    public Owner getById(Long ownerId) {
        return  ownerMapper.toModel(
                ownerRepository.findById(ownerId)
                        .orElseThrow(() -> new DbRecordNotFoundException("Owner not found with ID: " + ownerId)));

    }

    @Caching(
            evict = { @CacheEvict(value = "ownersCache", key = "#ownerId") },
            put = { @CachePut(value = "ownersCache", key = "#result.id") }
    )
    public Owner addAccountToOwner(Long ownerId, Currency currency) {
        Owner owner = this.getById(ownerId);

        for (Account account : owner.getAccounts()) {
            if (account.getCurrency().equals(currency)) {
                throw new OwnerActionInvalidException("Cannot possess more than one account with currency " + currency);
            }
        }

        Account accountToAdd = Account.builder()
                .owner(owner)
                .balance(BigDecimal.ZERO)
                .currency(currency)
                .build();

        accountRepository.save(accountMapper.toDbo(accountToAdd));

        return owner;
    }

    @CacheEvict(value = "ownersCache", key = "#ownerId")
    public void deleteOwner(Long ownerId) {
        ownerRepository.deleteById(ownerId);
    }
}
