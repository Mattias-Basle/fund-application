package com.example.fund_app.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.util.Set;

@Entity
@Table(name = "owners")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(
        name = "owner_generator",
        sequenceName = "owner_sequence",
        allocationSize = 1
)
@DynamicInsert
public class Owner {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "owner_generator")
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String username;

    @Column(name = "ACCOUNTS")
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Account> accounts;

    @Version
    @Column(name = "OWNER_LOCK_VERSION",nullable = false)
    @ColumnDefault("0")
    private Long version;
}
