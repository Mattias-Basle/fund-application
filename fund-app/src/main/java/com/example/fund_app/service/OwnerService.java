package com.example.fund_app.service;

import com.example.fund_app.exception.DbRecordNotFoundException;
import com.example.fund_app.exception.OwnerActionInvalidException;
import com.example.fund_app.exception.OwnerAlreadyExistsException;
import com.example.fund_app.model.Account;
import com.example.fund_app.model.Currency;
import com.example.fund_app.model.Owner;
import com.example.fund_app.repository.AccountRepository;
import com.example.fund_app.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final AccountRepository accountRepository;
    private final CacheManager ownerCacheManager;

    public OwnerService(OwnerRepository ownerRepository,
                        AccountRepository accountRepository,
                        @Qualifier("ownerCacheManager") CacheManager ownerCacheManager) {
        this.ownerRepository = ownerRepository;
        this.accountRepository = accountRepository;
        this.ownerCacheManager = ownerCacheManager;
    }

    public void createOwner(String username) {
        if (ownerRepository.existsByUsername(username)) {
            throw new OwnerAlreadyExistsException(username + " already exists");
        }

        Owner newOwner = Owner.builder()
                .username(username)
                .accounts(new HashSet<>())
                .build();

        ownerRepository.save(newOwner);
    }

    public List<Owner> getAllOwners() {
        return ownerRepository.findAll();
    }

    public Owner getById(Long ownerId) {
        Cache cache = ownerCacheManager.getCache("OWNER_CACHE");
        Owner cachedOwner = cache.get(ownerId, Owner.class);
        if (cachedOwner != null) {
            return cachedOwner;
        }
        Owner savedOwner =  ownerRepository.findById(ownerId)
                .orElseThrow(() -> new DbRecordNotFoundException("Owner not found with ID: " + ownerId));
        cache.put(ownerId, savedOwner);
        return savedOwner;
    }

    public void addAccountToOwner(Long ownerId, Currency currency) {
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

        accountRepository.save(accountToAdd);
        updateOwnerCache(ownerId);
    }

    @CacheEvict(cacheManager = "ownerCacheManager", value = "OWNER_CACHE", key = "#ownerId")
    public void deleteOwner(Long ownerId) {
        ownerRepository.deleteById(ownerId);
    }

    private void updateOwnerCache(Long ownerId) {
        Owner updatedOwner =  ownerRepository.findById(ownerId)
                .orElseThrow(() -> new DbRecordNotFoundException("Owner not found with ID: " + ownerId));

        Cache cache = ownerCacheManager.getCache("OWNER_CACHE");
        cache.evictIfPresent(ownerId);
        cache.put(updatedOwner.getId(), updatedOwner);
    }
}
