package com.example.fund_app.controller;

import com.example.fund_app.exception.DbRecordNotFoundException;
import com.example.fund_app.exception.OwnerActionInvalidException;
import com.example.fund_app.exception.OwnerAlreadyExistsException;
import com.example.fund_app.mapper.OwnerMapper;
import com.example.fund_app.model.Currency;
import com.example.fund_app.model.Owner;
import com.example.fund_app.model.dto.OwnerViewDto;
import com.example.fund_app.service.OwnerService;
import org.approvaltests.JsonApprovals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {OwnerController.class})
public class OwnerControllerTest extends AbstractCT {

    @MockitoBean
    private OwnerService ownerService;

    @MockitoBean
    private OwnerMapper ownerMapper;

    private final String BASE_URL = "/owners";

    @Test
    @DisplayName("should create an owner successfully")
    void createOwnerSuccesfully() throws Exception {
        // Given
        String name = "test";

        // When
        doNothing().when(ownerService).createOwner(name);

        // Then
        mockMvc.perform(post(BASE_URL)
                .param("name", name))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("should return 400 if owner already exists")
    void createOwnerFails() throws Exception {
        // Given
        String name = "test";

        // When
        doThrow(OwnerAlreadyExistsException.class).when(ownerService).createOwner(name);

        // Then
        mockMvc.perform(post(BASE_URL)
                        .param("name", name))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return empty list if no owner exists")
    void getAllOwnersEmpty() throws Exception {
        // Given
        // When
        doReturn(List.of()).when(ownerService).getAllOwners();

        // Then
        String responseBody = mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonApprovals.verifyJson(responseBody);
    }

    @Test
    @DisplayName("should return the expected list when fetching all owners")
    void getAllOwnersSuccessfully() throws Exception {
        // Given
        List<Owner> owners = List.of();
        Set<Long> accountIds = new HashSet<>(2);
        accountIds.add(2L);
        accountIds.add(3L);
        OwnerViewDto owner1 = new OwnerViewDto(1L, "test1", accountIds);
        OwnerViewDto owner2 = new OwnerViewDto(2L, "test2", Set.of(1L));

        // When
        doReturn(owners).when(ownerService).getAllOwners();
        doReturn(List.of(owner1, owner2)).when(ownerMapper).toDto(owners);

        // Then
        String responseBody = mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonApprovals.verifyJson(responseBody);
    }

    @Test
    @DisplayName("should return the owner successfully")
    void getOwnerByIdSuccessfully() throws Exception {
        // Given
        Long id = 1L;
        Owner owner = new Owner();
        Set<Long> accountIds = new HashSet<>(2);
        accountIds.add(3L);
        accountIds.add(7L);

        OwnerViewDto dto = new OwnerViewDto(1L, "test", accountIds);

        // When
        doReturn(owner).when(ownerService).getById(id);
        doReturn(dto).when(ownerMapper).toDto(owner);

        // Then
        String responseBody = mockMvc.perform(get(BASE_URL.concat("/1")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonApprovals.verifyJson(responseBody);
    }

    @Test
    @DisplayName("should return 404 if owner do not exists")
    void getOwnerByIdNotFound() throws Exception {
        // Given
        Long id = 1L;

        // When
        doThrow(DbRecordNotFoundException.class).when(ownerService).getById(id);

        // Then
        mockMvc.perform(get(BASE_URL.concat("/1")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should link a new account to the owner successfully")
    void addAccountSuccessfully() throws Exception {
        // Given
        Long id = 1L;
        Currency currency = Currency.EUR;

        // When
        doNothing().when(ownerService).addAccountToOwner(id, currency);

        // Then
        mockMvc.perform(patch(BASE_URL.concat("/1"))
                        .param("currency", currency.name()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("should return 209 if owner linked to an account with the same currency")
    void addAccountFails() throws Exception {
        // Given
        Long id = 1L;
        Currency currency = Currency.EUR;

        // When
        doThrow(OwnerActionInvalidException.class).when(ownerService).addAccountToOwner(id, currency);

        // Then
        mockMvc.perform(patch(BASE_URL.concat("/1"))
                        .param("currency", currency.name()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should delete owner successfully")
    void deleteSuccessfully() throws Exception {
        // Given
        Long id = 1L;

        // When
        doNothing().when(ownerService).deleteOwner(id);

        // Then
        mockMvc.perform(delete(BASE_URL.concat("/1")))
                .andExpect(status().isNoContent());
    }
}
