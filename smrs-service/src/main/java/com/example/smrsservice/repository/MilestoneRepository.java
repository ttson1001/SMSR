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

    // ✅ SỬA: Dùng native query với LIMIT 1
    @Query(value = "SELECT * FROM milestone WHERE project_id = :projectId AND is_final = :isFinal ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<Milestone> findFirstByProjectIdAndIsFinalOrderByIdDesc(
            @Param("projectId") Integer projectId,
            @Param("isFinal") Boolean isFinal
    );

    @Query(value = "SELECT * FROM milestone WHERE project_id = :projectId AND is_final = :isFinal ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<Milestone> findByProjectIdAndIsFinal(
            @Param("projectId") Integer projectId,
            @Param("isFinal") Boolean isFinal
    );

    List<Milestone> findAllByProjectIdAndIsFinal(Integer projectId, Boolean isFinal);

    List<Milestone> findByIsFinalAndStatus(Boolean isFinal, String status);

    List<Milestone> findByIsFinal(Boolean isFinal);

    List<Milestone> findByReportSubmittedById(Integer submitterId);

    @Query("SELECT m FROM Milestone m WHERE m.project.id = :projectId ORDER BY m.dueDate ASC")
    List<Milestone> findByProjectId(@Param("projectId") Integer projectId);
}