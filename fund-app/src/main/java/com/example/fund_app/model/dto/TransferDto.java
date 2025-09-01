package com.example.fund_app.model.dto;

import java.math.BigDecimal;

/**
 *
 * @param senderAccount account ID of the sender
 * @param receiverAccount account ID of the receiver
 * @param amount amount to be sent
 * @param toSend flag to determine if the amount should be the received or sent one
 *               (this will allow to determine the computation in case of exchange rates)
 */
public record TransferDto(
        Long senderAccount,
        Long receiverAccount,
        BigDecimal amount,
        boolean toSend
) {
}
