package com.example.fund_app.repository;

import com.example.fund_app.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    boolean existsByUsername(String username);
}
