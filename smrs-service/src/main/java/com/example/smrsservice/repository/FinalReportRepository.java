package com.example.smrsservice.repository;

import com.example.smrsservice.entity.FinalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinalReportRepository extends JpaRepository<FinalReport,Integer> {
    List<FinalReport> findByProjectId(Integer projectId);

    List<FinalReport> findBySubmittedById(Integer submittedById);

    Optional<FinalReport> findTopByProjectIdOrderByVersionDesc(Integer projectId);

    @Query("SELECT fr FROM FinalReport fr WHERE fr.project.id = :projectId ORDER BY fr.version DESC")
    List<FinalReport> findAllVersionsByProjectId(@Param("projectId") Integer projectId);

    @Query("SELECT fr FROM FinalReport fr WHERE fr.status = :status")
    List<FinalReport> findByStatus(@Param("status") String status);

    List<FinalReport> findByProjectIdOrderByVersionDesc(Integer projectId);
}
