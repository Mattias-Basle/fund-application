package com.example.fund_app.service;

import com.example.fund_app.exception.DbRecordNotFoundException;
import com.example.fund_app.exception.OwnerActionInvalidException;
import com.example.fund_app.exception.OwnerAlreadyExistsException;
import com.example.fund_app.model.Account;
import com.example.fund_app.model.Currency;
import com.example.fund_app.model.Owner;
import com.example.fund_app.repository.AccountRepository;
import com.example.fund_app.repository.OwnerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.data.domain.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OwnerServiceTest {

    @Mock
    private OwnerRepository ownerRepository;
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CacheManager ownerCacheManager;

    @InjectMocks
    private OwnerService ownerService;

    @Captor
    ArgumentCaptor<Owner> ownerCaptor;

    @Mock
    private Pageable pageable;

    @Test
    @DisplayName("should throw if owner already in DB")
    void createOwnerFails() {
        // Given
        String username = "test";

        // When
        doReturn(true).when(ownerRepository).existsByUsername(username);

        // Then
        assertThrows(OwnerAlreadyExistsException.class, () -> ownerService.createOwner(username));
        verify(ownerRepository, times(0)).save(any(Owner.class));
    }

    @Test
    @DisplayName("should create a new owner successfully")
    void createOwnerSuccessful() {
        // Given
        String username = "test";
        Owner owner = Owner.builder().id(1L).build();
        Cache cache = new ConcurrentMapCache("test");

        // When
        doReturn(false).when(ownerRepository).existsByUsername(username);
        doReturn(owner).when(ownerRepository).save(any());
        doReturn(cache).when(ownerCacheManager).getCache(any());


        // Then
        assertDoesNotThrow(() -> ownerService.createOwner(username));
        verify(ownerRepository, times(1)).save(ownerCaptor.capture());

        Owner savedEntity = ownerCaptor.getValue();
        assertEquals(username, savedEntity.getUsername());
    }

    @Test
    @DisplayName("should throw if owner not in DB")
    void getByIdFails() {
        // Given
        Long id = 1L;
        Cache cache = new ConcurrentMapCache("TEST_CACHE");

        // When
        doReturn(cache).when(ownerCacheManager).getCache(anyString());
        doReturn(Optional.empty()).when(ownerRepository).findById(id);

        // Then
        assertThrows(DbRecordNotFoundException.class, () -> ownerService.getById(id));
    }

    @Test
    @DisplayName("should return an empty slice if no owners")
    void getAllOwnersEmpty() {
        // Given
        Page<Owner> owners = new PageImpl<>(List.of());

        // When
        doReturn(owners).when(ownerRepository).findAll(pageable);

        // Then
        Page<Owner> result = ownerService.getAllOwners(pageable);
        assertFalse(result.hasContent());
    }

    @Test
    @DisplayName("should return a slice with owners paginated")
    void getAllOwnersWithDataPaginated() {
        // Given
        Page<Owner> owners = new PageImpl<>(List.of(
                new Owner(), new Owner(), new Owner()
        ));

        // When
        doReturn(owners).when(ownerRepository).findAll(pageable);

        // Then
        Page<Owner> result = ownerService.getAllOwners(pageable);
        assertTrue(result.hasContent());
        assertEquals(3, result.getContent().size());
    }

    @Test
    @DisplayName("should throw if owner already have an account with same currency")
    void addAccountFails() {
        // Given
        Long id = 1L;
        Currency currency = Currency.USD;
        Account account = Account.builder().currency(currency).build();
        Owner owner = new Owner(id, "test", Set.of(account), 0L);
        Cache cache = new ConcurrentMapCache("TEST_CACHE");

        // When
        doReturn(cache).when(ownerCacheManager).getCache(anyString());
        doReturn(Optional.of(owner)).when(ownerRepository).findById(id);

        // Then
        assertThrows(OwnerActionInvalidException.class, () -> ownerService.addAccountToOwner(id, currency));
    }

    @Test
    @DisplayName("should create new Account a link it to the owner properly")
    void addAccountSuccessful() {
        // Given
        Long id = 1L;
        Currency currency = Currency.USD;
        Owner owner = new Owner(id, "test", new HashSet<>(), 0L);
        Cache cache = new ConcurrentMapCache("TEST_CACHE");

        // When
        doReturn(cache).when(ownerCacheManager).getCache(anyString());
        doReturn(Optional.of(owner)).when(ownerRepository).findById(id);

        // Then
        assertDoesNotThrow(() -> ownerService.addAccountToOwner(id, currency));
        verify(accountRepository, times(1)).save(any(Account.class));
    }
}
