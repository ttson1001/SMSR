package com.example.smrsservice.repository;


import com.example.smrsservice.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface AccountRepository extends JpaRepository<Account,Integer>, JpaSpecificationExecutor<Account> {
    boolean existsByEmail(String email);
    @EntityGraph(attributePaths = {"role"})
    Optional<Account> findWithRoleByEmail(String email);
    @Query("SELECT a FROM Account a WHERE LOWER(a.email) = LOWER(:email)")
    Optional<Account> findByEmail(@Param("email") String email);
    @Query("SELECT COUNT(a) FROM Account a WHERE a.role.roleName = :roleName")
    long countByRoleName(@Param("roleName") String roleName);

    @Query("SELECT a FROM Account a WHERE a.role.roleName = :roleName")
    List<Account> findByRoleName(@Param("roleName") String roleName);





}
