package com.example.smrsservice.repository;


import com.example.smrsservice.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface AccountRepository extends JpaRepository<Account,Integer> {
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
    @EntityGraph(attributePaths = {"role"})
    Optional<Account> findWithRoleByEmail(String email);
}
