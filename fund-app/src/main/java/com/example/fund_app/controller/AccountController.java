package com.example.fund_app.controller;

import com.example.fund_app.mapper.AccountMapper;
import com.example.fund_app.model.Account;
import com.example.fund_app.model.dto.AccountViewDto;
import com.example.fund_app.model.dto.TransferDto;
import com.example.fund_app.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountViewDto> getAccountById(@PathVariable Long accountId) {
        Account account = accountService.findById(accountId);
        return ResponseEntity.ok(accountMapper.toDto(account));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long accountId) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<String> accountDeposit(
            @PathVariable Long accountId,
            @Min(10L) @RequestParam("amount") BigDecimal amount) {
        String result = accountService.deposit(accountId, amount);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<String> accountWithdrawal(
            @PathVariable Long accountId,
            @Min(10L) @RequestParam("amount") BigDecimal amount) {
        String result = accountService.withdraw(accountId, amount);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> accountTransfer( @Valid @RequestBody TransferDto dto) {
        if (dto.toSend()) {
            return ResponseEntity.ok(accountService.transferTo(dto.senderAccount(), dto.receiverAccount(), dto.amount()));
        }
        return ResponseEntity.ok(accountService.transferFrom(dto.senderAccount(), dto.receiverAccount(), dto.amount()));
    }
}
