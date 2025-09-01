package com.example.fund_app.feign;

import com.example.fund_app.model.Currency;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
@FeignClient(
        name = "${xrate.client.name}",
        url = "${xrate.client.url}"
)
public interface ExchangeRateClient {

    @GetMapping("/{currency}")
    ResponseEntity<ERApiResponse> fetchRatesPerCurrency(@PathVariable Currency currency);
}
