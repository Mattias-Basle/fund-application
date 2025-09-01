package com.example.fund_app.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "owners")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Owner {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String username;

    @Column(name = "ACCOUNTS")
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private Set<Account> accounts;
}
