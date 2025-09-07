package com.example.fund_app.service;

import com.example.fund_app.exception.AccountActionInvalidException;
import com.example.fund_app.exception.DbRecordNotFoundException;
import com.example.fund_app.mapper.AccountMapper;
import com.example.fund_app.model.Account;
import com.example.fund_app.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.lang.String.format;

@Service
@Slf4j
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;

    private final AccountMapper accountMapper;
    private final ExchangeRateService exchangeRateService;
    private final TransactionAuditService transactionService;

    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper,
                          ExchangeRateService exchangeRateService,
                          TransactionAuditService transactionService) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.exchangeRateService = exchangeRateService;
        this.transactionService = transactionService;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "accountsCache", key = "#accountId")
    public Account findById(Long accountId) {
        return accountMapper.toModel(
                accountRepository.findById(accountId)
                        .orElseThrow(() -> new DbRecordNotFoundException("Could not find account with ID: " + accountId)));
    }


    @CacheEvict(value = "accountsCache", key = "#accountId")
    public void deleteAccount(Long accountId) {
        accountRepository.deleteById(accountId);
        log.warn("Account {} successfully deleted", accountId);
    }

    @CacheEvict(value = "accountsCache", key = "#accountId")
    public String deposit(Long accountId, BigDecimal amount) {
        log.info("Starting a new deposit on account: {}", accountId);
        Account account = findById(accountId);
        return deposit(account, amount, true);
    }

    private String deposit(Account account, BigDecimal amount, boolean logTransaction) {
        BigDecimal newBalance = account.getBalance().add(amount);

        account.setBalance(newBalance);
        accountRepository.save(accountMapper.toDbo(account));

        if (logTransaction) {
            transactionService.logDeposit(account, amount);
        }

        return format("Successful deposit of %s %.2f for account %s. New balance is: %s %.2f",
                account.getCurrency().name(), amount, account.getAccountId(), account.getCurrency().name(), newBalance);
    }

    @CacheEvict(value = "accountsCache", key = "#accountId")
    public String withdraw(Long accountId, BigDecimal amount) {
        log.info("Starting a new withdrawal on account: {}", accountId);
        Account account = findById(accountId);
        return withdraw(account, amount, true);
    }

    private String withdraw(Account account, BigDecimal amount, boolean logTransaction) {
        BigDecimal newBalance = account.getBalance().subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountActionInvalidException("The account does not have sufficient funds for this operation");
        }

        account.setBalance(newBalance);
        accountRepository.save(accountMapper.toDbo(account));

        if (logTransaction) {
            transactionService.logWithdrawal(account, amount);
        }

        return format("Successfully withdrawn %s %.2f for account %s. New balance is: %s %.2f",
                account.getCurrency().name(), amount, account.getAccountId(), account.getCurrency().name(), newBalance);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "accountsCache", key = "#senderId"),
                    @CacheEvict(value = "accountsCache", key = "#receiverId")
            }
    )
    public String transferTo(Long senderId, Long receiverId, BigDecimal amount) {
        log.info("Starting a new transfer between {} and {}", senderId, receiverId);
        if (senderId.equals(receiverId)) {
            throw new AccountActionInvalidException("Transfer cannot be performed within the same account");
        }

        Account senderAccount = findById(senderId);

        if (amount.compareTo(senderAccount.getBalance()) > 0) {
            throw new AccountActionInvalidException("The account does not have sufficient funds for this operation");
        }

        Account receiverAccount = findById(receiverId);

        if (receiverAccount.getCurrency().equals(senderAccount.getCurrency())) {
            computeWithinCurrency(senderAccount, receiverAccount, amount);
        } else {
            computeOnDifferentCurrenciesToSend(senderAccount, receiverAccount, amount);
        }

        return format("Transfer between accounts %s and %s successful", senderId, receiverId);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "accountsCache", key = "#senderId"),
                    @CacheEvict(value = "accountsCache", key = "#receiverId")
            }
    )

    public String transferFrom(Long senderId, Long receiverId, BigDecimal amount) {
        log.info("Starting a new transfer between {} and {}", senderId, receiverId);
        if (senderId.equals(receiverId)) {
            throw new AccountActionInvalidException("Transfer cannot be performed within the same account");
        }

        Account senderAccount = findById(senderId);
        Account receiverAccount = findById(receiverId);

        BigDecimal amountToWithdraw;

        if (senderAccount.getCurrency().equals(receiverAccount.getCurrency())) {
            amountToWithdraw = amount;
            withdraw(senderAccount, amountToWithdraw, false);
        } else {
            BigDecimal rate = exchangeRateService.getRate(senderAccount.getCurrency(), receiverAccount.getCurrency());
            amountToWithdraw = amount.divide(rate, 2, RoundingMode.HALF_EVEN);

            withdraw(senderAccount, amountToWithdraw, false);
        }

        this.deposit(receiverAccount, amount, false);
        transactionService.logTransfer(senderAccount, receiverAccount, amountToWithdraw, amount);

        return format("Transfer between accounts %s and %s successful", senderId, receiverId);
    }

    private void computeWithinCurrency(Account senderAccount, Account receiverAccount, BigDecimal amount) {
        withdraw(senderAccount, amount, false);
        deposit(receiverAccount, amount, false);
        transactionService.logTransfer(senderAccount, receiverAccount, amount, amount);
    }

    private void computeOnDifferentCurrenciesToSend(Account senderAccount, Account receiverAccount, BigDecimal amount) {
        BigDecimal rate = exchangeRateService.getRate(senderAccount.getCurrency(), receiverAccount.getCurrency());

        BigDecimal amountToDeposit = amount.multiply(rate);

        withdraw(senderAccount, amount, false);
        deposit(receiverAccount, amountToDeposit, false);

        transactionService.logTransfer(senderAccount, receiverAccount, amount, amountToDeposit);
    }
}
