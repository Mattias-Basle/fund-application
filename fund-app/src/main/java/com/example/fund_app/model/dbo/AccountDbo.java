package com.example.fund_app.model.dbo;

import com.example.fund_app.model.Currency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@SequenceGenerator(
        name = "account_generator",
        sequenceName = "account_sequence",
        allocationSize = 1,
        initialValue = 1000
)
public class AccountDbo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_generator")
    @Column(name = "ACCOUNT_ID")
    private Long accountId;

    @ManyToOne
    @JoinColumn(name = "OWNER_ID", nullable = false)
    private OwnerDbo owner;

    @Column(name = "CURRENCY", nullable = false)
    private Currency currency;

    @Column(name = "BALANCE")
    private BigDecimal balance;

    @Version
    @Column(name = "ACCOUNT_LOCK_VERSION", nullable = false)
    @ColumnDefault("0")
    private Long version;
}
