package com.example.fund_app.controller;

import com.example.fund_app.exception.DbRecordNotFoundException;
import com.example.fund_app.exception.OwnerActionInvalidException;
import com.example.fund_app.exception.OwnerAlreadyExistsException;
import com.example.fund_app.mapper.OwnerMapper;
import com.example.fund_app.model.Currency;
import com.example.fund_app.model.Owner;
import com.example.fund_app.model.dto.AccountDetailsViewDto;
import com.example.fund_app.model.dto.OwnerDetailsViewDto;
import com.example.fund_app.model.dto.OwnerViewDto;
import com.example.fund_app.service.OwnerService;
import com.google.gson.Gson;
import org.approvaltests.JsonApprovals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.*;

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
    @Mock
    private Pageable pageable;

    private Gson gson = new Gson();

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
    @DisplayName("should not create owner if name does not follow regex pattern")
    void createOwnerFailsRegex() throws Exception {
        // Given
        String name1 = "t";
        String name2 = "testWithNameTooLong";
        String name3 = "testWith12";


        // Too short for regex
        mockMvc.perform(post(BASE_URL)
                        .param("name", name1))
                .andExpect(status().isBadRequest());
        // Too long for regex
        mockMvc.perform(post(BASE_URL)
                        .param("name", name2))
                .andExpect(status().isBadRequest());
        // Contains digits
        mockMvc.perform(post(BASE_URL)
                        .param("name", name3))
                .andExpect(status().isBadRequest());
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
    @DisplayName("should return empty page if no owner exists")
    void getAllOwnersEmpty() throws Exception {
        // Given
        Page<Owner> owners = new PageImpl<>(List.of(new Owner()));
        Page<OwnerViewDto> views = new PageImpl<>(List.of());
        // When
        doReturn(owners).when(ownerService).getAllOwners(any());
        doReturn(views).when(ownerMapper).toDto(owners);

        // Then
        String responseBody = mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> response = gson.fromJson(responseBody, Map.class);
        List<Owner> responseOwners = (List<Owner>) response.get("content");
        JsonApprovals.verifyJson(responseOwners.toString());
    }

    @Test
    @DisplayName("should return 400 if request pagination is not adequate")
    void getAllOwnersFails() throws Exception {
        // Try to ask less than 10 results per page
        mockMvc.perform(get(BASE_URL.concat("?size=5")))
                .andExpect(status().isBadRequest());
        // Try to ask more than 100 results per page
        mockMvc.perform(get(BASE_URL.concat("?size=150")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return the expected list when fetching all owners")
    void getAllOwnersSuccessfully() throws Exception {
        // Given
        List<Owner> owners = List.of();
        Page<Owner> pagedOwners = new PageImpl<>(owners);
        Set<Long> accountIds = new HashSet<>(2);
        accountIds.add(2L);
        accountIds.add(3L);
        OwnerViewDto owner1 = new OwnerViewDto(1L, "test1", accountIds);
        OwnerViewDto owner2 = new OwnerViewDto(2L, "test2", Set.of(1L));

        Page<OwnerViewDto> pagedView = new PageImpl<>(List.of(owner1, owner2));

        // When
        doReturn(pagedOwners).when(ownerService).getAllOwners(any(Pageable.class));
        doReturn(pagedView).when(ownerMapper).toDto(pagedOwners);

        // Then
        String responseBody = mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> response = gson.fromJson(responseBody, Map.class);
        List<Owner> responseOwners = (List<Owner>) response.get("content");
        JsonApprovals.verifyJson(responseOwners.toString());
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
    @DisplayName("should return owner details successfully")
    void getOwnerDetailsByIdSuccessfully() throws Exception {
        // Given
        Long id = 1L;
        Owner owner = new Owner();

        AccountDetailsViewDto account1Details = new AccountDetailsViewDto(1L, Currency.EUR, BigDecimal.ONE);
        AccountDetailsViewDto account2Details = new AccountDetailsViewDto(5L, Currency.USD, BigDecimal.TEN);

        Set<AccountDetailsViewDto> accountsDetails = new LinkedHashSet<>(2);
        accountsDetails.add(account1Details);
        accountsDetails.add(account2Details);

        OwnerDetailsViewDto dto = new OwnerDetailsViewDto(1L, "test", accountsDetails);

        // When
        doReturn(owner).when(ownerService).getById(id);
        doReturn(dto).when(ownerMapper).toDetailsDto(owner);

        // Then
        String responseBody = mockMvc.perform(get(BASE_URL.concat("/1/details")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonApprovals.verifyJson(responseBody);
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
