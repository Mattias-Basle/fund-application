package com.example.fund_app.service;

import com.example.fund_app.model.Account;
import com.example.fund_app.model.Amount;
import com.example.fund_app.model.Transaction;
import com.example.fund_app.model.TransactionType;
import com.example.fund_app.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void logDeposit(Account depositAccount, BigDecimal value) {
        Amount depositAmount = Amount.builder()
                .currency(depositAccount.getCurrency())
                .value(value)
                .build();

        Transaction transaction = Transaction.builder()
                .sender(depositAccount)
                .amountReceived(depositAmount)
                .type(TransactionType.DEPOSIT)
                .transactionDate(Instant.now())
                .build();

        transactionRepository.save(transaction);
    }

    public void logWithdrawal(Account withdrawalAccount, BigDecimal value) {
        Amount depositAmount = Amount.builder()
                .currency(withdrawalAccount.getCurrency())
                .value(value)
                .build();

        Transaction transaction = Transaction.builder()
                .sender(withdrawalAccount)
                .amountSent(depositAmount)
                .type(TransactionType.WITHDRAWAL)
                .transactionDate(Instant.now())
                .build();

        transactionRepository.save(transaction);
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
                .sender(sender)
                .receiver(receiver)
                .amountSent(amountSent)
                .amountReceived(amountReceived)
                .type(TransactionType.TRANSFER)
                .transactionDate(Instant.now())
                .build();

        transactionRepository.save(transaction);
    }
}
