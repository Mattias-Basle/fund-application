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

import java.util.HashSet;
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

    @InjectMocks
    private OwnerService ownerService;

    @Captor
    ArgumentCaptor<Owner> ownerCaptor;

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

        // When
        doReturn(false).when(ownerRepository).existsByUsername(username);

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

        // When
        doReturn(Optional.empty()).when(ownerRepository).findById(id);

        // Then
        assertThrows(DbRecordNotFoundException.class, () -> ownerService.getById(id));
    }

    @Test
    @DisplayName("should throw if owner already have an account with same currency")
    void addAccountFails() {
        // Given
        Long id = 1L;
        Currency currency = Currency.USD;
        Account account = Account.builder().currency(currency).build();
        Owner owner = new Owner(id, "test", Set.of(account));

        // When
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
        Account account = Account.builder().currency(currency).build();
        Owner owner = new Owner(id, "test", new HashSet<>());

        // When
        doReturn(Optional.of(owner)).when(ownerRepository).findById(id);
        doReturn(account).when(accountRepository).save(any(Account.class));

        // Then
        assertDoesNotThrow(() -> ownerService.addAccountToOwner(id, currency));
        verify(ownerRepository, times(1)).save(ownerCaptor.capture());

        Owner updatedEntity = ownerCaptor.getValue();
        assertEquals(1, updatedEntity.getAccounts().size());
    }
}
