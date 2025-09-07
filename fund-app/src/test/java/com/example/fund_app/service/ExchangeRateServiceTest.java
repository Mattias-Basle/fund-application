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
    private ExchangeRateClient exchangeRateClient;

    @Mock
    private ExchangeRateMapper exchangeRateMapper;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    @DisplayName("should return the rate from the DB")
    void rateIsStoredInDB() {
        // Given
        Currency in = Currency.BRL;
        Currency out = Currency.EUR;
        ExchangeRate exchangeRate = ExchangeRate.builder()
                .currency(in)
                .rates(Map.of(out, BigDecimal.TWO))
                .lastUpdatedAt(LocalDate.now())
                .build();

        // When
        doReturn(Optional.of(exchangeRate)).when(exchangeRateRepository)
                .findById(eq(in));

        // Then
        BigDecimal result = exchangeRateService.getRate(in, out);
        assertEquals(BigDecimal.TWO, result);

        verify(exchangeRateRepository, times(1)).findById(any());
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
                .lastUpdatedAt(LocalDate.now())
                .build();

        ERApiResponse response = new ERApiResponse("success", "", Map.of());

        // When
        doReturn(Optional.empty()).when(exchangeRateRepository)
                .findById(eq(in));
        doReturn(ResponseEntity.ofNullable(response)).when(exchangeRateClient).fetchRatesPerCurrency(in);
        doReturn(exchangeRate).when(exchangeRateMapper).toEntity(eq(response), any(LocalDate.class));

        // Then
        BigDecimal result = exchangeRateService.getRate(in, out);
        assertEquals(BigDecimal.TWO, result);

        verify(exchangeRateRepository, times(1)).findById(any());
        verify(exchangeRateClient, times(1)).fetchRatesPerCurrency(any(Currency.class));
    }

    @Test
    @DisplayName("should return the rate from the external API if currency rates has not been updated")
    void rateIsFetchedWhenRecordIsTooOld() {
        // Given
        Currency in = Currency.BRL;
        Currency out = Currency.EUR;
        ExchangeRate exchangeRate = ExchangeRate.builder()
                .currency(in)
                .rates(Map.of(out, BigDecimal.TWO))
                .lastUpdatedAt(LocalDate.now().minusDays(2))
                .build();

        ERApiResponse response = new ERApiResponse("success", "", Map.of(out.name(), BigDecimal.TEN));

        // When
        doReturn(Optional.of(exchangeRate)).when(exchangeRateRepository)
                .findById(eq(in));
        doReturn(ResponseEntity.ofNullable(response)).when(exchangeRateClient).fetchRatesPerCurrency(in);

        // Then
        BigDecimal result = exchangeRateService.getRate(in, out);
        assertEquals(BigDecimal.TEN, result);

        verify(exchangeRateRepository, times(1)).findById(any());
        verify(exchangeRateClient, times(1)).fetchRatesPerCurrency(any(Currency.class));
    }

    @Test
    @DisplayName("should throw if fetching rate fails to retrieve the desired currency")
    void fetchingRateFailsOnCurrency() {
        // Given
        Currency in = Currency.BRL;
        Currency out = Currency.EUR;

        ERApiResponse response = new ERApiResponse("fail", "", Map.of());

        // When
        doReturn(Optional.empty()).when(exchangeRateRepository)
                .findById(eq(in));
        doReturn(ResponseEntity.ofNullable(response)).when(exchangeRateClient).fetchRatesPerCurrency(in);

        // Then
        assertThrows(ExchangeRateNotRetrievableException.class, () -> exchangeRateService.getRate(in, out));

        verify(exchangeRateRepository, times(1)).findById(any());
        verify(exchangeRateClient, times(1)).fetchRatesPerCurrency(any(Currency.class));
    }

    @Test
    @DisplayName("should throw if fetching rate API returns an error")
    void fetchingRateFailsOnApiCall() {
        // Given
        Currency in = Currency.BRL;
        Currency out = Currency.EUR;

        // When
        doReturn(Optional.empty()).when(exchangeRateRepository)
                .findById(eq(in));
        doReturn(ResponseEntity.internalServerError().build()).when(exchangeRateClient).fetchRatesPerCurrency(in);

        // Then
        assertThrows(ExchangeRateNotRetrievableException.class, () -> exchangeRateService.getRate(in, out));

        verify(exchangeRateRepository, times(1)).findById(any());
        verify(exchangeRateClient, times(1)).fetchRatesPerCurrency(any(Currency.class));
    }
}
