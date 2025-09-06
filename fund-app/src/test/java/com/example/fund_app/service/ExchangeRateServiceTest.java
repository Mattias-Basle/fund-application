package com.example.fund_app.service;

import com.example.fund_app.exception.ExchangeRateNotRetrievableException;
import com.example.fund_app.feign.ERApiResponse;
import com.example.fund_app.feign.ExchangeRateClient;
import com.example.fund_app.mapper.ExchangeRateMapper;
import com.example.fund_app.model.Currency;
import com.example.fund_app.model.ExchangeRate;
import com.example.fund_app.repository.ExchangeRateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private CacheManager exchangeRateCacheManager;

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @Mock
    private ExchangeRateMapper exchangeRateMapper;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    @DisplayName("should return the rate from the cache")
    void rateIsCached() {
        // Given
        Currency in = Currency.BRL;
        Currency out = Currency.EUR;
        ExchangeRate exchangeRate = ExchangeRate.builder()
                .currency(in)
                .rates(Map.of(out, BigDecimal.TWO))
                .lastUpdatedAt(LocalDate.now())
                .build();

        Cache cache = new ConcurrentMapCache("TEST");
        cache.put(in, exchangeRate);

        // When
        doReturn(cache).when(exchangeRateCacheManager).getCache(any());

        // Then
        BigDecimal result = exchangeRateService.getRate(in, out);
        assertEquals(BigDecimal.TWO, result);

        verify(exchangeRateRepository, times(0)).findByCurrencyAndLastUpdatedAt(any(), any());
        verify(exchangeRateClient, times(0)).fetchRatesPerCurrency(any(Currency.class));
    }

    @Test
    @DisplayName("should return the rate from the DB")
    void rateIsStoredInDB() {
        // Given
        Currency in = Currency.BRL;
        Currency out = Currency.EUR;
        ExchangeRate exchangeRate = ExchangeRate.builder()
                .currency(in)
                .rates(Map.of(out, BigDecimal.TWO))
                .build();

        Cache cache = new ConcurrentMapCache("TEST");

        // When
        doReturn(cache).when(exchangeRateCacheManager).getCache(any());
        doReturn(Optional.of(exchangeRate)).when(exchangeRateRepository)
                .findByCurrencyAndLastUpdatedAt(eq(in), any());

        // Then
        BigDecimal result = exchangeRateService.getRate(in, out);
        assertEquals(BigDecimal.TWO, result);

        verify(exchangeRateRepository, times(1)).findByCurrencyAndLastUpdatedAt(any(), any());
        verify(exchangeRateClient, times(0)).fetchRatesPerCurrency(any(Currency.class));
    }

    @Test
    @DisplayName("should return the rate from the external API")
    void rateIsFetched() {
        // Given
        Currency in = Currency.BRL;
        Currency out = Currency.EUR;
        ExchangeRate exchangeRate = ExchangeRate.builder()
                .currency(in)
                .rates(Map.of(out, BigDecimal.TWO))
                .build();

        Cache cache = new ConcurrentMapCache("TEST");

        ERApiResponse response = new ERApiResponse("success", "", Map.of());

        // When
        doReturn(cache).when(exchangeRateCacheManager).getCache(any());
        doReturn(Optional.empty()).when(exchangeRateRepository)
                .findByCurrencyAndLastUpdatedAt(eq(in), any());
        doReturn(ResponseEntity.ofNullable(response)).when(exchangeRateClient).fetchRatesPerCurrency(in);
        doReturn(exchangeRate).when(exchangeRateMapper).toEntity(eq(response), any(LocalDate.class));
        doReturn(exchangeRate).when(exchangeRateRepository).save(exchangeRate);

        // Then
        BigDecimal result = exchangeRateService.getRate(in, out);
        assertEquals(BigDecimal.TWO, result);

        verify(exchangeRateRepository, times(1)).findByCurrencyAndLastUpdatedAt(any(), any());
        verify(exchangeRateClient, times(1)).fetchRatesPerCurrency(any(Currency.class));
    }

    @Test
    @DisplayName("should throw if fetching rate fails to retrieve the desired currency")
    void fetchingRateFailsOnCurrency() {
        // Given
        Currency in = Currency.BRL;
        Currency out = Currency.EUR;
        Cache cache = new ConcurrentMapCache("TEST");

        ERApiResponse response = new ERApiResponse("fail", "", Map.of());

        // When
        doReturn(cache).when(exchangeRateCacheManager).getCache(any());
        doReturn(Optional.empty()).when(exchangeRateRepository)
                .findByCurrencyAndLastUpdatedAt(eq(in), any());
        doReturn(ResponseEntity.ofNullable(response)).when(exchangeRateClient).fetchRatesPerCurrency(in);

        // Then
        assertThrows(ExchangeRateNotRetrievableException.class, () -> exchangeRateService.getRate(in, out));

        verify(exchangeRateRepository, times(1)).findByCurrencyAndLastUpdatedAt(any(), any());
        verify(exchangeRateClient, times(1)).fetchRatesPerCurrency(any(Currency.class));
    }

    @Test
    @DisplayName("should throw if fetching rate API returns an error")
    void fetchingRateFailsOnApiCall() {
        // Given
        Currency in = Currency.BRL;
        Currency out = Currency.EUR;
        Cache cache = new ConcurrentMapCache("TEST");

        // When
        doReturn(cache).when(exchangeRateCacheManager).getCache(any());
        doReturn(Optional.empty()).when(exchangeRateRepository)
                .findByCurrencyAndLastUpdatedAt(eq(in), any());
        doReturn(ResponseEntity.internalServerError().build()).when(exchangeRateClient).fetchRatesPerCurrency(in);

        // Then
        assertThrows(ExchangeRateNotRetrievableException.class, () -> exchangeRateService.getRate(in, out));

        verify(exchangeRateRepository, times(1)).findByCurrencyAndLastUpdatedAt(any(), any());
        verify(exchangeRateClient, times(1)).fetchRatesPerCurrency(any(Currency.class));
    }
}
