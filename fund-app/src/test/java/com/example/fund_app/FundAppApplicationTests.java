package com.example.fund_app;

import com.example.fund_app.model.Currency;
import com.example.fund_app.model.Owner;
import com.example.fund_app.model.dto.AccountViewDto;
import com.example.fund_app.model.dto.OwnerViewDto;
import com.example.fund_app.model.dto.TransferDto;
import com.example.fund_app.repository.AccountRepository;
import com.example.fund_app.repository.OwnerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
class FundAppApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OwnerRepository ownerRepository;

	@Autowired
	private AccountRepository accountRepository;

	@MockitoSpyBean
	private OwnerRepository ownerRepositorySpy;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		ownerRepository.deleteAll();
	}

	@Test
	@Rollback
	@DisplayName("should mimic the whole life cycle flow of an owner")
	void createOwnerStoresInDB_ThenGetOwnerStoredInCache_ThenOwnerUpdatesDBAndCache_ThenOwnerDeleteDBAndEvictCache() throws Exception {
		// Run creation of new owner
		mockMvc.perform(post("/owners")
						.param("name", "test"));

		// Retrieve owner ID
		String responseBody = mockMvc.perform(get("/owners"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
		List mappedResponse = (List) response.get("content");
		OwnerViewDto firstOwner = objectMapper.convertValue(mappedResponse.getFirst(), OwnerViewDto.class);
		Long ownerId = firstOwner.id();

		// Check that owner is store in DB
		assertTrue(ownerRepository.existsById(ownerId));

		// Then call to retrieve owner
		mockMvc.perform(get("/owners/" + ownerId));

		// Check that created owner is still in cache
		verify(ownerRepositorySpy, times(0)).findById(ownerId);

		// Call to update owner with an account
		Currency currency = Currency.USD;
		mockMvc.perform(patch("/owners/" + ownerId)
				.param("currency", currency.name()));


		// Then call to retrieve owner again and put it back into cache
		String responseBody2 = mockMvc.perform(get("/owners/" + ownerId))
				.andReturn()
				.getResponse()
				.getContentAsString();

		OwnerViewDto dto = objectMapper.readValue(responseBody2, OwnerViewDto.class);
		assertNotNull(dto);
		assertFalse(dto.accountIds().isEmpty());

		// Call to delete owner
		mockMvc.perform(delete("/owners/" + ownerId));



		// Check owner has been deleted from DB
		String responseBody3 = mockMvc.perform(get("/owners"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		Map<String, Object> response2 = objectMapper.readValue(responseBody3, Map.class);
		List<Owner> mappedResponse2 = (List<Owner>) response2.get("content");

		assertTrue(mappedResponse2.isEmpty());
	}

	@Test
	@Rollback
	@DisplayName("should mimic the whole life cycle of an account")
	void createAccount_ThenGetAccountStoredInCache_ThenAccountUpdateDBAndCache_ThenAccountDeleteDBAndEvictCache() throws Exception {
		// Run creation of new owner
		mockMvc.perform(post("/owners")
				.param("name", "test"));

		// Retrieve owner ID
		String responseBody = mockMvc.perform(get("/owners"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
		List mappedResponse = (List) response.get("content");
		OwnerViewDto firstOwner = objectMapper.convertValue(mappedResponse.getFirst(), OwnerViewDto.class);
		Long ownerId = firstOwner.id();

		// Check that owner is store in DB
		assertTrue(ownerRepository.existsById(ownerId));

		// Run creation of an account
		mockMvc.perform(patch("/owners/" + ownerId)
				.param("currency", "EUR"));

		// Retrieve account ID
		String responseBody2 = mockMvc.perform(get("/owners/" + ownerId))
				.andReturn()
				.getResponse()
				.getContentAsString();

		OwnerViewDto firstOwner2 = objectMapper.readValue(responseBody2, OwnerViewDto.class);
		Long accountId = firstOwner2.accountIds().stream().findFirst().get();

		// Check that account is store in DB
		assertTrue(accountRepository.existsById(accountId));


		// Then deposit money
		mockMvc.perform(post("/accounts/" + accountId + "/deposit")
				.param("amount", "20"));

		// Then call to retrieve updated account
		String reponseBody3 = mockMvc.perform(get("/accounts/" + accountId))
				.andReturn().getResponse().getContentAsString();

		AccountViewDto dto = objectMapper.readValue(reponseBody3, AccountViewDto.class);
		assertEquals(new BigDecimal("20.00"), dto.balance());


		// Then withdraw money
		mockMvc.perform(post("/accounts/" + accountId + "/withdraw")
				.param("amount", "10"));

		// Then call to retrieve updated account
		String reponseBody4 = mockMvc.perform(get("/accounts/" + accountId))
				.andReturn().getResponse().getContentAsString();

		AccountViewDto dto2 = objectMapper.readValue(reponseBody4, AccountViewDto.class);
		assertEquals(new BigDecimal("10.00"), dto2.balance());

		// Delete account
		mockMvc.perform(delete("/accounts/" + accountId));

		// Assert deletion
		mockMvc.perform(get("/accounts/" + accountId))
				.andExpect(status().isNotFound());
	}

	@Test
	@Rollback
	@DisplayName("should transfer funds properly")
	void createOwnersAndAccounts_ThenTransferSameCurrency_ThenTransferOtherCurrency() throws Exception {
		// Set up first owner
		mockMvc.perform(post("/owners")
				.param("name", "first"));
		String responseBody1 = mockMvc.perform(get("/owners"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		Map<String, Object> response1 = objectMapper.readValue(responseBody1, Map.class);
		List mappedResponse1 = (List) response1.get("content");
		OwnerViewDto firstOwner = objectMapper.convertValue(mappedResponse1.getFirst(), OwnerViewDto.class);
		Long ownerId1 = firstOwner.id();


		// Run creation of an account in EUR for 1st
		mockMvc.perform(patch("/owners/" + ownerId1)
				.param("currency", "EUR"));
		String responseBody2 = mockMvc.perform(get("/owners/" + ownerId1))
				.andReturn()
				.getResponse()
				.getContentAsString();

		OwnerViewDto firstOwner2 = objectMapper.readValue(responseBody2, OwnerViewDto.class);
		Long accountId1EUR = firstOwner2.accountIds().stream().findFirst().get();

		// Deposit initial amount for transfers
		mockMvc.perform(post("/accounts/" + accountId1EUR + "/deposit")
				.param("amount", "100"));

		// Run creation of an account in BRL for 1st
		mockMvc.perform(patch("/owners/" + ownerId1)
				.param("currency", "BRL"));
		String responseBody3 = mockMvc.perform(get("/owners/" + ownerId1))
				.andReturn()
				.getResponse()
				.getContentAsString();

		OwnerViewDto firstOwner3 = objectMapper.readValue(responseBody3, OwnerViewDto.class);
		Long accountId1BRL = firstOwner3.accountIds().stream()
				.filter(id -> !id.equals(accountId1EUR)).findFirst().get();


		// Set up second owner
		mockMvc.perform(post("/owners")
				.param("name", "second"));
		String responseBody4 = mockMvc.perform(get("/owners"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		Map<String, Object> response4 = objectMapper.readValue(responseBody4, Map.class);
		List mappedResponse4 = (List) response4.get("content");
		OwnerViewDto secondOwner = objectMapper.convertValue(mappedResponse4.get(1), OwnerViewDto.class);
		Long ownerId2 = secondOwner.id();

		// Run creation of an account in EUR for 2nd
		mockMvc.perform(patch("/owners/" + ownerId2)
				.param("currency", "EUR"));
		String responseBody5 = mockMvc.perform(get("/owners/" + ownerId2))
				.andReturn()
				.getResponse()
				.getContentAsString();

		OwnerViewDto secondOwner2 = objectMapper.readValue(responseBody5, OwnerViewDto.class);
		Long accountId2EUR = secondOwner2.accountIds().stream().findFirst().get();

		// Transfer in same currency from first to second
		TransferDto dto1 = new TransferDto(
				accountId1EUR,
				accountId2EUR,
				BigDecimal.valueOf(80),
				true
		);
		mockMvc.perform(post("/accounts/transfer")
				.content(objectMapper.writeValueAsString(dto1))
				.contentType(MediaType.APPLICATION_JSON));

		// Then check balances
		String balanceFirstOnFirstTransfer = mockMvc.perform(get("/accounts/" + accountId1EUR))
				.andReturn()
				.getResponse()
				.getContentAsString();

		AccountViewDto view1 = objectMapper.readValue(balanceFirstOnFirstTransfer, AccountViewDto.class);
		assertEquals(new BigDecimal("20.00"), view1.balance());

		String balanceSecondOnFirstTransfer = mockMvc.perform(get("/accounts/" + accountId2EUR))
				.andReturn()
				.getResponse()
				.getContentAsString();

		AccountViewDto view2 = objectMapper.readValue(balanceSecondOnFirstTransfer, AccountViewDto.class);
		assertEquals(new BigDecimal("80.00"), view2.balance());

		// Transfer from second to first in the other currency with sender as reference
		TransferDto dto2 = new TransferDto(
				accountId2EUR,
				accountId1BRL,
				BigDecimal.valueOf(20),
				true
		);
		mockMvc.perform(post("/accounts/transfer")
				.content(objectMapper.writeValueAsString(dto2))
				.contentType(MediaType.APPLICATION_JSON));

		// Then check balances again
		String balanceSecondOnSecondTransfer = mockMvc.perform(get("/accounts/" + accountId2EUR))
				.andReturn()
				.getResponse()
				.getContentAsString();

		AccountViewDto view3 = objectMapper.readValue(balanceSecondOnSecondTransfer, AccountViewDto.class);
		assertEquals(new BigDecimal("60.00"), view3.balance());

		String balanceFirstOnSecondTransfer = mockMvc.perform(get("/accounts/" + accountId1BRL))
				.andReturn()
				.getResponse()
				.getContentAsString();

		AccountViewDto view4 = objectMapper.readValue(balanceFirstOnSecondTransfer, AccountViewDto.class);

		// Store the new BRL balance for next transfer check
		BigDecimal BalanceBRL = view4.balance();
		assertEquals(-1, BigDecimal.ZERO.compareTo(BalanceBRL));

		// Transfer from second to first in the other currency with receiver as reference
		TransferDto dto3 = new TransferDto(
				accountId2EUR,
				accountId1BRL,
				BigDecimal.valueOf(20),
				false
		);
		mockMvc.perform(post("/accounts/transfer")
				.content(objectMapper.writeValueAsString(dto3))
				.contentType(MediaType.APPLICATION_JSON));

		// Check the final balances
		String balanceSecondOnThirdTransfer = mockMvc.perform(get("/accounts/" + accountId2EUR))
				.andReturn()
				.getResponse()
				.getContentAsString();

		AccountViewDto view5 = objectMapper.readValue(balanceSecondOnThirdTransfer, AccountViewDto.class);
		BigDecimal oldBalance = new BigDecimal("60.00");
		assertEquals(1, oldBalance.compareTo(view5.balance()));


		String balanceFirstOnThirdTransfer = mockMvc.perform(get("/accounts/" + accountId1BRL))
				.andReturn()
				.getResponse()
				.getContentAsString();

		AccountViewDto view6 = objectMapper.readValue(balanceFirstOnThirdTransfer, AccountViewDto.class);

		// Store the new BRL balance for next transfer check
		BigDecimal addedValue = BigDecimal.valueOf(20);
		BigDecimal expectedBalance = BalanceBRL.add(addedValue);

		assertEquals(expectedBalance.setScale(2, RoundingMode.HALF_EVEN),view6.balance());
	}
}
