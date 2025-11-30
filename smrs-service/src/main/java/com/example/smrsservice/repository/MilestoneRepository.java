package com.example.smrsservice.repository;


import com.example.smrsservice.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Integer> {
    List<Milestone> findByProject_Id(Integer projectId);

    boolean existsByProjectIdAndIsFinalTrue(Integer projectId);

    Optional<Milestone> findByProjectIdAndIsFinalTrue(Integer projectId);

    Optional<Milestone> findByProjectIdAndIsFinal(Integer projectId, Boolean isFinal);

    List<Milestone> findByIsFinalAndStatus(Boolean isFinal, String status);

    List<Milestone> findByIsFinal(Boolean isFinal);

    List<Milestone> findByReportSubmittedById(Integer submitterId);

    @Query("SELECT m FROM Milestone m WHERE m.project.id = :projectId ORDER BY m.dueDate ASC")
    List<Milestone> findByProjectId(@Param("projectId") Integer projectId);
}
