package com.example.fund_app.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.EmbeddableInstantiatorRegistrations;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private String id;

    @Column(name = "TYPE", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TransactionType type;

    @ManyToOne
    @JoinColumn(name = "SENDER_ID")
    private Account sender;

    @ManyToOne
    @JoinColumn(name = "RECEIVER_ID")
    private Account receiver;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "currency", column = @Column(name = "SENT_CURRENCY")),
            @AttributeOverride( name = "value", column = @Column(name = "SENT_VALUE")),
    })
    private Amount amountSent;

    @Embedded
    @Column(name = "AMOUNT_RECEIVED")
    private Amount amountReceived;

    @Column(name = "TRANSACTION_DATE", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Instant transactionDate;
}
