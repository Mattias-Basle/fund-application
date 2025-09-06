package com.example.fund_app.repository;

import com.example.fund_app.model.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {
                    "accountId",
                    "owner",
                    "owner.id",
                    "currency",
                    "balance"
            }
    )
    Optional<Account> findById(Long id);
}
