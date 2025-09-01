package com.example.fund_app;

import com.example.fund_app.model.Currency;
import com.example.fund_app.model.Owner;
import com.example.fund_app.repository.OwnerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.Assert.assertNull;
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
	@Qualifier("ownerCacheManager")
	private CacheManager cacheManager;

	@Autowired
	private OwnerRepository ownerRepository;

	@MockitoSpyBean
	private OwnerRepository ownerRepositorySpy;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		ownerRepository.deleteAll();
	}

	@Test
	@DisplayName("should mimic the whole life cycle flow of an owner")
	void createOwnerStoresInDB_ThenGetOwnerStoresInCache_ThenOwnerUpdatesDBAndCache_ThenOwnerDeleteDBAndEvictCache() throws Exception {
		// Run creation of new owner
		mockMvc.perform(post("/owners")
						.param("name", "test"));

		// Check that owner is store in DB
		assertTrue(ownerRepository.existsById(1L));

		// Then call to retrieve owner
		mockMvc.perform(get("/owners/1"));

		// Check cache
		Cache cache = cacheManager.getCache("OWNER_CACHE");
		assertNotNull(cache);

		// Call again to make sure DB is only called once on first retrieval
		mockMvc.perform(get("/owners/1"));

		verify(ownerRepositorySpy, times(1)).findById(1L);

		// Call to update owner with an account
		Currency currency = Currency.USD;
		mockMvc.perform(patch("/owners/1")
				.param("currency", currency.name()));

		// Check cache is updated for owner
		Owner updatedOwner = cache.get(1L, Owner.class);

        assertFalse(updatedOwner.getAccounts().isEmpty());

		// Call to delete owner
		mockMvc.perform(delete("/owners/1"));

		// Check owner is evicted from cache
		Owner evictedOwner = cache.get(1L, Owner.class);
		assertNull(evictedOwner);

		// Check owner has been deleted from DB
		String responseBody = mockMvc.perform(get("/owners"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		List mappedResponse = objectMapper.readValue(responseBody, List.class);

		assertTrue(mappedResponse.isEmpty());
	}
}
