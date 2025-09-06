package com.example.fund_app.model;

import lombok.*;

import java.time.Instant;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private String id;

    private TransactionType type;

    private Account sender;

    private Account receiver;

    private Amount amountSent;

    private Amount amountReceived;

    private Instant transactionDate;
}
