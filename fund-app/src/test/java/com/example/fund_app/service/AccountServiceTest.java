package com.example.fund_app.service;

import com.example.fund_app.exception.AccountActionInvalidException;
import com.example.fund_app.exception.DbRecordNotFoundException;
import com.example.fund_app.mapper.AccountMapper;
import com.example.fund_app.model.Account;
import com.example.fund_app.model.Currency;
import com.example.fund_app.model.dbo.AccountDbo;
import com.example.fund_app.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AccountService accountService;

    @Captor
    ArgumentCaptor<AccountDbo> accountDboCaptor;

    @Captor
    ArgumentCaptor<Account> accountModelCaptor;

    @Test
    @DisplayName("should find account in db and return it")
    void findByIdSuccessful() {
        // Given
        Long id = 1L;
        AccountDbo dbo = new AccountDbo();
        Account account = Account.builder()
                .accountId(id)
                .build();

        // When
        doReturn(Optional.of(dbo)).when(accountRepository).findById(id);
        doReturn(account).when(accountMapper).toModel(dbo);

        // Then
        Account result = accountService.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getAccountId());
        verify(accountRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("should throw if account not in db")
    void findByIdFails() {
        // Given
        Long id = 1L;

        // When
        doReturn(Optional.empty()).when(accountRepository).findById(id);

        // Then
        assertThrows(DbRecordNotFoundException.class, () -> accountService.findById(id));
    }

    @Test
    @DisplayName("should deposit money and successfully update balance")
    void depositSuccessful() {
        // Given
        Long id = 1L;
        BigDecimal deposit = BigDecimal.TEN;
        AccountDbo dbo = new AccountDbo();
        dbo.setBalance(deposit);
        Account account = Account.builder()
                .accountId(id)
                .balance(BigDecimal.ZERO)
                .currency(Currency.USD)
                .build();



        // When
        doReturn(Optional.of(dbo)).when(accountRepository).findById(1L);
        doReturn(account).when(accountMapper).toModel(dbo);
        doReturn(dbo).when(accountMapper).toDbo(any());
        doReturn(dbo).when(accountRepository).save(any());

        // Then
        assertDoesNotThrow(() -> accountService.deposit(id, deposit));
        verify(transactionService, times(1)).logDeposit(any(), any());
        verify(accountRepository, times(1)).save(accountDboCaptor.capture());

        AccountDbo savedEntity = accountDboCaptor.getValue();
        assertNotNull(savedEntity);
    }

    @Test
    @DisplayName("should withdraw money and successfully update balance")
    void withdrawSuccessful() {
        // Given
        Long id = 1L;
        AccountDbo dbo = new AccountDbo();
        Account account = Account.builder()
                .accountId(id)
                .balance(BigDecimal.TEN)
                .currency(Currency.USD)
                .build();

        BigDecimal deposit = BigDecimal.TEN;

        // When
        doReturn(Optional.of(dbo)).when(accountRepository).findById(1L);
        doReturn(account).when(accountMapper).toModel(dbo);
        doReturn(dbo).when(accountMapper).toDbo(any());
        doReturn(dbo).when(accountRepository).save(any());

        // Then
        assertDoesNotThrow(() -> accountService.withdraw(id, deposit));
        verify(transactionService, times(1)).logWithdrawal(any(), any());
        verify(accountRepository, times(1)).save(accountDboCaptor.capture());

        AccountDbo savedEntity = accountDboCaptor.getValue();
        assertNotNull(savedEntity);
    }

    @Test
    @DisplayName("should throw error if balance lower than withdrawal amount")
    void withdrawFails() {
        // Given
        Long id = 1L;
        AccountDbo dbo = new AccountDbo();
        Account account = Account.builder()
                .accountId(id)
                .balance(BigDecimal.TWO)
                .currency(Currency.USD)
                .build();

        BigDecimal deposit = BigDecimal.TEN;

        // When
        doReturn(Optional.of(dbo)).when(accountRepository).findById(1L);
        doReturn(account).when(accountMapper).toModel(dbo);

        // Then
        assertThrows(AccountActionInvalidException.class, () -> accountService.withdraw(id, deposit));

        verify(transactionService, times(0)).logWithdrawal(any(), any());
        verify(accountRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("should fail transfer if both sender and receiver accounts are the same")
    void transferFailsIfSameAccounts() {
        // Given
        Long id = 1L;

        // Then
        assertThrows(AccountActionInvalidException.class, () -> accountService.transferTo(id, id, null));
        assertThrows(AccountActionInvalidException.class, () -> accountService.transferFrom(id, id, null));
    }

    @Test
    @DisplayName("should succeed at transfering with the same currency using sender as amount reference")
    void transferToSuccessfulWithinSameCurrency() {
        // Given
        Long id1 = 1L;
        Long id2 = 2L;

        AccountDbo sender = AccountDbo.builder().accountId(id1).balance(BigDecimal.TEN).currency(Currency.USD).build();
        AccountDbo receiver = AccountDbo.builder().accountId(id2).balance(BigDecimal.TWO).currency(Currency.USD).build();

        Account senderAcc = Account.builder().accountId(id1).balance(BigDecimal.TEN).currency(Currency.USD).build();
        Account receiverAcc = Account.builder().accountId(id1).balance(BigDecimal.TWO).currency(Currency.USD).build();

        // When
        doReturn(Optional.of(sender)).when(accountRepository).findById(id1);
        doReturn(Optional.of(receiver)).when(accountRepository).findById(id2);
        doReturn(senderAcc).when(accountMapper).toModel(sender);
        doReturn(receiverAcc).when(accountMapper).toModel(receiver);
        doReturn(sender).when(accountMapper).toDbo(senderAcc);
        doReturn(receiver).when(accountMapper).toDbo(receiverAcc);
        doReturn(sender).when(accountRepository).save(sender);
        doReturn(receiver).when(accountRepository).save(receiver);

        // Then
        assertDoesNotThrow(() -> accountService.transferTo(id1, id2, BigDecimal.TEN));
        verify(transactionService, times(1)).logTransfer(
                accountModelCaptor.capture(), accountModelCaptor.capture(), any(BigDecimal.class), any(BigDecimal.class)
        );

        List<Account> updatedAccounts = accountModelCaptor.getAllValues();
        assertEquals(2, updatedAccounts.size());
        assertEquals(BigDecimal.ZERO, updatedAccounts.get(0).getBalance());
        assertEquals(BigDecimal.valueOf(12L), updatedAccounts.get(1).getBalance());
    }

    @Test
    @DisplayName("should succeed at transfering with the same currency using receiver as amount reference")
    void transferFromSuccessfulWithinSameCurrency() {
        // Given
        Long id1 = 1L;
        Long id2 = 2L;

        AccountDbo sender = AccountDbo.builder().accountId(id1).balance(BigDecimal.TEN).currency(Currency.USD).build();
        AccountDbo receiver = AccountDbo.builder().accountId(id2).balance(BigDecimal.TWO).currency(Currency.USD).build();

        Account senderAcc = Account.builder().accountId(id1).balance(BigDecimal.TEN).currency(Currency.USD).build();
        Account receiverAcc = Account.builder().accountId(id1).balance(BigDecimal.TWO).currency(Currency.USD).build();

        // When
        doReturn(Optional.of(sender)).when(accountRepository).findById(id1);
        doReturn(Optional.of(receiver)).when(accountRepository).findById(id2);
        doReturn(senderAcc).when(accountMapper).toModel(sender);
        doReturn(receiverAcc).when(accountMapper).toModel(receiver);
        doReturn(sender).when(accountMapper).toDbo(senderAcc);
        doReturn(receiver).when(accountMapper).toDbo(receiverAcc);
        doReturn(sender).when(accountRepository).save(sender);
        doReturn(receiver).when(accountRepository).save(receiver);

        // Then
        assertDoesNotThrow(() -> accountService.transferFrom(id1, id2, BigDecimal.TEN));
        verify(transactionService, times(1)).logTransfer(
                accountModelCaptor.capture(), accountModelCaptor.capture(), any(BigDecimal.class), any(BigDecimal.class)
        );

        List<Account> updatedAccounts = accountModelCaptor.getAllValues();
        assertEquals(2, updatedAccounts.size());
        assertEquals(BigDecimal.ZERO, updatedAccounts.get(0).getBalance());
        assertEquals(BigDecimal.valueOf(12), updatedAccounts.get(1).getBalance());
    }

    @Test
    @DisplayName("should succeed at transfering with different currencies using sender as amount reference")
    void transferToSuccessfulWithDifferentCurrency() {
        // Given
        Long id1 = 1L;
        Long id2 = 2L;

        AccountDbo sender = AccountDbo.builder().accountId(id1).balance(BigDecimal.TEN).currency(Currency.USD).build();
        AccountDbo receiver = AccountDbo.builder().accountId(id2).balance(BigDecimal.TWO).currency(Currency.EUR).build();

        Account senderAcc = Account.builder().accountId(id1).balance(BigDecimal.TEN).currency(Currency.USD).build();
        Account receiverAcc = Account.builder().accountId(id1).balance(BigDecimal.TWO).currency(Currency.EUR).build();

        // When
        doReturn(Optional.of(sender)).when(accountRepository).findById(id1);
        doReturn(Optional.of(receiver)).when(accountRepository).findById(id2);
        doReturn(senderAcc).when(accountMapper).toModel(sender);
        doReturn(receiverAcc).when(accountMapper).toModel(receiver);
        doReturn(sender).when(accountMapper).toDbo(senderAcc);
        doReturn(receiver).when(accountMapper).toDbo(receiverAcc);
        doReturn(BigDecimal.TWO).when(exchangeRateService).getRate(Currency.USD, Currency.EUR);
        doReturn(sender).when(accountRepository).save(sender);
        doReturn(receiver).when(accountRepository).save(receiver);

        // Then
        assertDoesNotThrow(() -> accountService.transferTo(id1, id2, BigDecimal.TEN));
        verify(transactionService, times(1)).logTransfer(
                accountModelCaptor.capture(), accountModelCaptor.capture(), any(BigDecimal.class), any(BigDecimal.class)
        );

        List<Account> updatedAccounts = accountModelCaptor.getAllValues();
        assertEquals(2, updatedAccounts.size());
        assertEquals(BigDecimal.ZERO, updatedAccounts.get(0).getBalance());
        assertEquals(BigDecimal.valueOf(22), updatedAccounts.get(1).getBalance());
    }

    @Test
    @DisplayName("should succeed at transfering with different currencies using receiver as amount reference")
    void transferFromSuccessfulWithDifferentCurrency() {
        // Given
        Long id1 = 1L;
        Long id2 = 2L;

        AccountDbo sender = AccountDbo.builder().accountId(id1).balance(BigDecimal.TEN).currency(Currency.USD).build();
        AccountDbo receiver = AccountDbo.builder().accountId(id2).balance(BigDecimal.TWO).currency(Currency.EUR).build();

        Account senderAcc = Account.builder().accountId(id1).balance(BigDecimal.TEN).currency(Currency.USD).build();
        Account receiverAcc = Account.builder().accountId(id1).balance(BigDecimal.TWO).currency(Currency.EUR).build();

        // When
        doReturn(Optional.of(sender)).when(accountRepository).findById(id1);
        doReturn(Optional.of(receiver)).when(accountRepository).findById(id2);
        doReturn(senderAcc).when(accountMapper).toModel(sender);
        doReturn(receiverAcc).when(accountMapper).toModel(receiver);
        doReturn(sender).when(accountMapper).toDbo(senderAcc);
        doReturn(receiver).when(accountMapper).toDbo(receiverAcc);
        doReturn(BigDecimal.TWO).when(exchangeRateService).getRate(Currency.USD, Currency.EUR);
        doReturn(sender).when(accountRepository).save(sender);
        doReturn(receiver).when(accountRepository).save(receiver);

        // Then
        assertDoesNotThrow(() -> accountService.transferFrom(id1, id2, BigDecimal.TEN));
        verify(transactionService, times(1)).logTransfer(
                accountModelCaptor.capture(), accountModelCaptor.capture(), any(BigDecimal.class), any(BigDecimal.class)
        );

        List<Account> updatedAccounts = accountModelCaptor.getAllValues();
        assertEquals(2, updatedAccounts.size());
        assertEquals(BigDecimal.valueOf(500, 2), updatedAccounts.get(0).getBalance());
        assertEquals(BigDecimal.valueOf(12), updatedAccounts.get(1).getBalance());
    }
}
