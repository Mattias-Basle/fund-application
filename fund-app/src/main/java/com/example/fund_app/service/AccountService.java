package com.example.fund_app.service;

import com.example.fund_app.exception.AccountActionInvalidException;
import com.example.fund_app.exception.DbRecordNotFoundException;
import com.example.fund_app.model.Account;
import com.example.fund_app.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.lang.String.format;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    @Qualifier("accountCacheManager")
    private final CacheManager accountCacheManager;
    private final ExchangeRateService exchangeRateService;
    private final TransactionService transactionService;

    public AccountService(AccountRepository accountRepository, CacheManager cacheManager, ExchangeRateService exchangeRateService, TransactionService transactionService) {
        this.accountRepository = accountRepository;
        this.accountCacheManager = cacheManager;
        this.exchangeRateService = exchangeRateService;
        this.transactionService = transactionService;
    }

    public Account findById(Long accountId) {
        Cache cache = accountCacheManager.getCache("ACCOUNT_CACHE");
        Account cachedAccount = cache.get(accountId, Account.class);
        if (cachedAccount != null) {
            return cachedAccount;
        }
        Account savedAccount =  accountRepository.findById(accountId)
                .orElseThrow(() -> new DbRecordNotFoundException("Account not found with ID: " + accountId));
        cache.put(accountId, savedAccount);
        return savedAccount;
    }

    @CacheEvict(cacheManager = "accountCacheManager", value = "ACCOUNT_CACHE", key = "#accountId")
    public void deleteAccount(Long accountId) {
        accountRepository.deleteById(accountId);
    }

    public String deposit(Long accountId, BigDecimal amount, boolean logTransaction) {
        Account account = this.findById(accountId);
        BigDecimal newBalance = account.getBalance().add(amount);

        account.setBalance(newBalance);
        accountRepository.save(account);
        updateAccountCache(account);

        if (logTransaction) {
            transactionService.logDeposit(account, amount);
        }

        return format("Successful deposit of %s %f for account %s. New balance is: %s %.2f",
                account.getCurrency().name(), amount, accountId, account.getCurrency().name(), newBalance);
    }

    public String withdraw(Long accountId, BigDecimal amount, boolean logTransaction) {
        Account account = this.findById(accountId);
        BigDecimal newBalance = account.getBalance().subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountActionInvalidException("The account does not have sufficient funds for this operation");
        }

        account.setBalance(newBalance);
        accountRepository.save(account);
        updateAccountCache(account);

        if (logTransaction) {
            transactionService.logWithdrawal(account, amount);
        }

        return format("Successfully withdrawn %s %f for account %s. New balance is: %s %.2f",
                account.getCurrency().name(), amount, accountId, account.getCurrency().name(), newBalance);
    }

    public String transferTo(Long senderId, Long receiverId, BigDecimal amount) {
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


    public String transferFrom(Long senderId, Long receiverId, BigDecimal amount) {
        Account senderAccount = findById(senderId);
        Account receiverAccount = findById(receiverId);

        BigDecimal amountToWithdraw;

        if (senderAccount.getCurrency().equals(receiverAccount.getCurrency())) {
            amountToWithdraw = amount;
            this.withdraw(senderId, amountToWithdraw, false);
        } else {
            BigDecimal rate = exchangeRateService.getRate(senderAccount.getCurrency(), receiverAccount.getCurrency());
            amountToWithdraw = senderAccount.getBalance().divide(rate, RoundingMode.HALF_DOWN);

            this.withdraw(senderId, amountToWithdraw, false);

        }

        this.deposit(receiverId, amount, false);
        transactionService.logTransfer(senderAccount, receiverAccount, amountToWithdraw, amount);

        return format("Transfer between accounts %s and %s successful", senderId, receiverId);
    }

    private void computeWithinCurrency(Account senderAccount, Account receiverAccount, BigDecimal amount) {
        this.withdraw(senderAccount.getId(), amount, false);
        this.deposit(receiverAccount.getId(), amount, false);
        transactionService.logTransfer(senderAccount, receiverAccount, amount, amount);
    }

    private void computeOnDifferentCurrenciesToSend(Account senderAccount, Account receiverAccount, BigDecimal amount) {
        BigDecimal rate = exchangeRateService.getRate(senderAccount.getCurrency(), receiverAccount.getCurrency());


        BigDecimal amountToDeposit = amount.multiply(rate);

        this.withdraw(senderAccount.getId(), amount, false);
        this.deposit(receiverAccount.getId(), amountToDeposit, false);

        transactionService.logTransfer(senderAccount, receiverAccount, amount, amountToDeposit);
    }
    private void updateAccountCache(Account account) {
        Cache cache = accountCacheManager.getCache("ACCOUNT_CACHE");
        cache.evictIfPresent(account.getId());
        cache.put(account.getId(), account);
    }
}
