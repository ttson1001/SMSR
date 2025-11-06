package com.example.smrsservice.repository;

import com.example.smrsservice.entity.CouncilManagerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouncilManagerProfileRepository extends JpaRepository<CouncilManagerProfile,Integer> {
    Optional<CouncilManagerProfile> findByAccountId(Integer accountId);

    Optional<CouncilManagerProfile> findByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCode(String employeeCode);
}
