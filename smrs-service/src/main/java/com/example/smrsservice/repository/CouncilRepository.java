package com.example.smrsservice.repository;

import com.example.smrsservice.entity.Council;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouncilRepository extends JpaRepository<Council, Integer> {
    Optional<Council> findByCouncilCode(String councilCode);

    List<Council> findByDeanId(Integer deanId);

    List<Council> findByDepartment(String department);

    List<Council> findByStatus(String status);

    @Query("SELECT c FROM Council c WHERE c.dean.id = :deanId AND c.status = :status")
    List<Council> findByDeanIdAndStatus(@Param("deanId") Integer deanId,
                                        @Param("status") String status);

}
