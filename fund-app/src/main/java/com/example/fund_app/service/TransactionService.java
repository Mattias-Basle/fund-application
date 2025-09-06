package com.example.fund_app.service;

import com.example.fund_app.model.Account;
import com.example.fund_app.model.Amount;
import com.example.fund_app.model.Transaction;
import com.example.fund_app.model.TransactionType;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


/**
 * This service aims at auditing the transactions that occurs in accounts.
 * Currently developed as a simple logging platform, it can be updated to fit in event-driven architecture
 * by simply adding an event producer.
 */
@Service
@Slf4j
public class TransactionService {

    public void logDeposit(Account depositAccount, BigDecimal value) {
        Amount depositAmount = Amount.builder()
                .currency(depositAccount.getCurrency())
                .value(value)
                .build();

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .receiver(depositAccount)
                .amountReceived(depositAmount)
                .type(TransactionType.DEPOSIT)
                .transactionDate(Instant.now())
                .build();

        log.info("[FUND_APP] New deposit performed: {}", transaction.toString());
    }

    public void logWithdrawal(Account withdrawalAccount, BigDecimal value) {
        Amount depositAmount = Amount.builder()
                .currency(withdrawalAccount.getCurrency())
                .value(value)
                .build();

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .sender(withdrawalAccount)
                .amountSent(depositAmount)
                .type(TransactionType.WITHDRAWAL)
                .transactionDate(Instant.now())
                .build();

        log.info("[FUND_APP] New withdrawal performed: {}", transaction.toString());
    }

    public void logTransfer(Account sender, Account receiver, BigDecimal valueOut, BigDecimal valueIn) {
        Amount amountSent = Amount.builder()
                .currency(sender.getCurrency())
                .value(valueOut)
                .build();

        Amount amountReceived = Amount.builder()
                .currency(receiver.getCurrency())
                .value(valueIn)
                .build();

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .sender(sender)
                .receiver(receiver)
                .amountSent(amountSent)
                .amountReceived(amountReceived)
                .type(TransactionType.TRANSFER)
                .transactionDate(Instant.now())
                .build();

        log.info("[FUND_APP] New transfer performed: {}", transaction.toString());
    }
}
