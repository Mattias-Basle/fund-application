package com.example.fund_app.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Serializable {

    private Long accountId;

    @JsonBackReference
    private Owner owner;

    private Currency currency;

    private BigDecimal balance;

    private Long version;
}
