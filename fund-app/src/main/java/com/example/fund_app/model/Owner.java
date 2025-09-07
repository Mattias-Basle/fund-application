package com.example.fund_app.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.io.Serializable;
import java.util.Set;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Owner implements Serializable {

    private Long id;

    private String username;

    @JsonManagedReference
    private Set<Account> accounts;

    private Long version;
}
