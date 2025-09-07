package com.example.fund_app.service;

import com.example.fund_app.exception.DbRecordNotFoundException;
import com.example.fund_app.exception.OwnerActionInvalidException;
import com.example.fund_app.exception.OwnerAlreadyExistsException;
import com.example.fund_app.mapper.AccountMapper;
import com.example.fund_app.mapper.OwnerMapper;
import com.example.fund_app.model.Account;
import com.example.fund_app.model.Currency;
import com.example.fund_app.model.Owner;
import com.example.fund_app.model.dbo.AccountDbo;
import com.example.fund_app.model.dbo.OwnerDbo;
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
    private OwnerMapper ownerMapper;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private OwnerService ownerService;

    @Captor
    ArgumentCaptor<Owner> ownerModelCaptor;

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
        verify(ownerRepository, times(0)).save(any(OwnerDbo.class));
    }

    @Test
    @DisplayName("should create a new owner successfully")
    void createOwnerSuccessful() {
        // Given
        String username = "test";
        Owner owner = Owner.builder().id(1L).build();
        OwnerDbo dbo = new OwnerDbo();

        // When
        doReturn(false).when(ownerRepository).existsByUsername(username);
        doReturn(dbo).when(ownerRepository).save(any());
        doReturn(owner).when(ownerMapper).toModel(dbo);


        // Then
        assertDoesNotThrow(() -> ownerService.createOwner(username));
        verify(ownerRepository, times(1)).save(any());
        verify(ownerMapper, times(1)).toDbo(ownerModelCaptor.capture());

        Owner savedEntity = ownerModelCaptor.getValue();
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
    @DisplayName("should return an empty page if no owners")
    void getAllOwnersEmpty() {
        // Given
        Page<OwnerDbo> dbos = new PageImpl<>(List.of());
        Page<Owner> owners = new PageImpl<>(List.of());

        // When
        doReturn(dbos).when(ownerRepository).findAll(pageable);
        doReturn(owners).when(ownerMapper).toModel(dbos);

        // Then
        Page<Owner> result = ownerService.getAllOwners(pageable);
        assertFalse(result.hasContent());
    }

    @Test
    @DisplayName("should return a page with owners paginated")
    void getAllOwnersWithDataPaginated() {
        // Given
        Page<Owner> owners = new PageImpl<>(List.of(
                new Owner(), new Owner(), new Owner()
        ));

        Page<OwnerDbo> ownersDbo = new PageImpl<>(List.of(
                new OwnerDbo(), new OwnerDbo(), new OwnerDbo()
        ));

        // When
        doReturn(ownersDbo).when(ownerRepository).findAll(pageable);
        doReturn(owners).when(ownerMapper).toModel(ownersDbo);

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
        AccountDbo accountDbo = AccountDbo.builder().currency(currency).build();
        OwnerDbo ownerDbo = new OwnerDbo(id, "test", Set.of(accountDbo), 0L);

        Account account = Account.builder().currency(currency).build();
        Owner owner = Owner.builder().id(id).username("tset").accounts(Set.of(account)).build();

        // When
        doReturn(Optional.of(ownerDbo)).when(ownerRepository).findById(id);
        doReturn(owner).when(ownerMapper).toModel(ownerDbo);

        // Then
        assertThrows(OwnerActionInvalidException.class, () -> ownerService.addAccountToOwner(id, currency));
    }

    @Test
    @DisplayName("should create new Account a link it to the owner properly")
    void addAccountSuccessful() {
        // Given
        Long id = 1L;
        Currency currency = Currency.USD;
        OwnerDbo ownerDbo = new OwnerDbo(id, "test", new HashSet<>(), 0L);
        Owner owner = new Owner(id, "test", new HashSet<>(), 0L);

        AccountDbo account = new AccountDbo();

        // When
        doReturn(Optional.of(ownerDbo)).when(ownerRepository).findById(id);
        doReturn(owner).when(ownerMapper).toModel(ownerDbo);
        doReturn(account).when(accountMapper).toDbo(any());

        // Then
        assertDoesNotThrow(() -> ownerService.addAccountToOwner(id, currency));
        verify(accountRepository, times(1)).save(any(AccountDbo.class));
    }
}
