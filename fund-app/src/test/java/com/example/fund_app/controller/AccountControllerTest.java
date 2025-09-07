package com.example.fund_app.controller;

import com.example.fund_app.exception.DbRecordNotFoundException;
import com.example.fund_app.mapper.AccountMapper;
import com.example.fund_app.model.Account;
import com.example.fund_app.model.Currency;
import com.example.fund_app.model.dto.AccountViewDto;
import com.example.fund_app.model.dto.TransferDto;
import com.example.fund_app.service.AccountService;
import com.google.gson.Gson;
import org.approvaltests.JsonApprovals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AccountController.class})
public class AccountControllerTest extends AbstractCT {

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private AccountMapper accountMapper;

    private final String BASE_URL = "/accounts";

    private final Gson gson = new Gson();

    @Test
    @DisplayName("should return a view of the account successfully")
    void getAccountSuccessfully() throws Exception {
        // Given
        Account account = new Account();

        AccountViewDto dto = new AccountViewDto(Currency.USD, BigDecimal.TEN);

        // When
        doReturn(account).when(accountService).findById(1L);
        doReturn(dto).when(accountMapper).toDto(account);

        // Then
        String responseBody = mockMvc.perform(get(BASE_URL.concat("/1")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonApprovals.verifyJson(responseBody);
    }

    @Test
    @DisplayName("should return 404 if the account does not exists")
    void getAccountNotFound() throws Exception {
        // When
        doThrow(DbRecordNotFoundException.class).when(accountService).findById(1L);

        // Then
        mockMvc.perform(get(BASE_URL.concat("/1")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should delete account successfully")
    void deleteAccountSuccessfully() throws Exception {
        // When
        doNothing().when(accountService).deleteAccount(1L);

        // Then
        mockMvc.perform(delete(BASE_URL.concat("/1")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("should fail deposit and withdrawal if amount is lower than 10")
    void accountTransationFails() throws Exception {
        // Deposit
        mockMvc.perform(post(BASE_URL.concat("/1/deposit"))
                        .param("amount", "2"))
                .andExpect(status().isBadRequest());

        // Withdraw
        mockMvc.perform(post(BASE_URL.concat("/1/withdraw"))
                        .param("amount", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should deposit successfully")
    void accountDepositSuccessfully() throws Exception {
        // Given
        String confirmation = "Money successfully add to account";

        // When
        doReturn(confirmation).when(accountService).deposit(1L, BigDecimal.TEN);

        // Then
        String result = mockMvc.perform(post(BASE_URL.concat("/1/deposit"))
                        .param("amount", "10"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(confirmation, result);
    }

    @Test
    @DisplayName("should withdraw successfully")
    void accountWithdrawSuccessfully() throws Exception {
        // Given
        String confirmation = "Money successfully withdrawn to account";

        // When
        doReturn(confirmation).when(accountService).withdraw(1L, BigDecimal.TEN);

        // Then
        String result = mockMvc.perform(post(BASE_URL.concat("/1/withdraw"))
                        .param("amount", "10"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(confirmation, result);
    }

    @Test
    @DisplayName("should return 400 if dto not correctly prepared")
    void accountTransferFails() throws Exception {
        // Given
        TransferDto dto1 = new TransferDto(null, 2L, BigDecimal.TEN, true);
        TransferDto dto2 = new TransferDto(1L, null, BigDecimal.TEN, true);
        TransferDto dto3 = new TransferDto(1L, 2L, null, true);
        TransferDto dto4 = new TransferDto(1L, 2L, BigDecimal.TWO, true);
        String dto5 = """
                {
                    "senderAccount":1,
                    "receiverAccount":2,
                    "amount":20,
                    "toSend:null
                }
                """;

        // Sender account is null
        mockMvc.perform(post(BASE_URL.concat("/transfer"))
                        .content(gson.toJson(dto1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Receiver account is null
        mockMvc.perform(post(BASE_URL.concat("/transfer"))
                        .content(gson.toJson(dto2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Amount is null
        mockMvc.perform(post(BASE_URL.concat("/transfer"))
                        .content(gson.toJson(dto3))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Amount is less than 10
        mockMvc.perform(post(BASE_URL.concat("/transfer"))
                        .content(gson.toJson(dto4))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // toSend flag is null
        mockMvc.perform(post(BASE_URL.concat("/transfer"))
                        .content(dto5)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should transfer successfully using sender as amount reference")
    void accountTransferToSuccessful() throws Exception {
        // Given
        TransferDto dto = new TransferDto(1L, 2L, BigDecimal.TEN, true);
        String requestBody = gson.toJson(dto);

        //Then
        mockMvc.perform(post(BASE_URL.concat("/transfer"))
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(accountService, times(1)).transferTo(any(), any(), any());
        verify(accountService, times(0)).transferFrom(any(), any(), any());
    }

    @Test
    @DisplayName("should transfer successfully using receiver as amount reference")
    void accountTransferFromSuccessful() throws Exception {
        // Given
        TransferDto dto = new TransferDto(1L, 2L, BigDecimal.TEN, false);
        String requestBody = gson.toJson(dto);

        //Then
        mockMvc.perform(post(BASE_URL.concat("/transfer"))
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(accountService, times(0)).transferTo(any(), any(), any());
        verify(accountService, times(1)).transferFrom(any(), any(), any());
    }
}
