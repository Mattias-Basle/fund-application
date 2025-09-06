package com.example.fund_app.service;

import com.example.fund_app.model.Account;
import com.example.fund_app.model.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("should log deposit successfully")
    void logDepositSuccessful() {
        // Given
        BigDecimal depositValue = BigDecimal.TEN;

        Account account = Account.builder()
                .accountId(1L)
                .currency(Currency.BRL)
                .balance(depositValue)
                .build();

        // Then
        assertDoesNotThrow(() -> transactionService.logDeposit(account, depositValue));
    }

    @Test
    @DisplayName("should log withdrawal successfully")
    void logWithdrawalSuccessful() {
        // Given
        BigDecimal depositValue = BigDecimal.TEN;

        Account account = Account.builder()
                .accountId(1L)
                .currency(Currency.BRL)
                .balance(depositValue)
                .build();

        // Then
        assertDoesNotThrow(() -> transactionService.logWithdrawal(account, depositValue));
    }

    @Test
    @DisplayName("should log transfer successfully")
    void logTransferSuccessful() {
        // Given
        BigDecimal depositValue = BigDecimal.TEN;

        Account accountSender = Account.builder()
                .accountId(1L)
                .currency(Currency.BRL)
                .balance(depositValue)
                .build();

        BigDecimal receivedValue = BigDecimal.TWO;

        Account accountReceiver = Account.builder()
                .accountId(2L)
                .currency(Currency.EUR)
                .balance(receivedValue)
                .build();

        // Then
        assertDoesNotThrow(() -> transactionService.logTransfer(accountSender, accountReceiver, depositValue, receivedValue));
    }
}
