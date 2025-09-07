package com.example.fund_app.repository;

import com.example.fund_app.model.dbo.OwnerDbo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerRepository extends JpaRepository<OwnerDbo, Long> {

    boolean existsByUsername(String username);

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {
                    "id",
                    "username",
                    "accounts",
                    "accounts.accountId"
            }
    )
    Page<OwnerDbo> findAll(Pageable pageable);
}
