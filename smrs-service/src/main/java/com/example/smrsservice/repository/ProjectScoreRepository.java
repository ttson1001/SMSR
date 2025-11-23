package com.example.smrsservice.repository;

import com.example.smrsservice.entity.ProjectScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectScoreRepository extends JpaRepository<ProjectScore, Integer> {
    List<ProjectScore> findByProjectId(Integer projectId);

    List<ProjectScore> findByFinalReportId(Integer finalReportId);

    List<ProjectScore> findByLecturerId(Integer lecturerId);

    Optional<ProjectScore> findByProjectIdAndLecturerId(Integer projectId, Integer lecturerId);

    Optional<ProjectScore> findByFinalReportIdAndLecturerId(Integer finalReportId, Integer lecturerId);

    @Query("SELECT AVG(ps.finalScore) FROM ProjectScore ps WHERE ps.project.id = :projectId")
    Double getAverageScoreByProjectId(@Param("projectId") Integer projectId);

    @Query("SELECT AVG(ps.finalScore) FROM ProjectScore ps WHERE ps.finalReport.id = :finalReportId")
    Double getAverageScoreByFinalReportId(@Param("finalReportId") Integer finalReportId);

    boolean existsByProjectIdAndLecturerId(Integer projectId, Integer lecturerId);

    boolean existsByFinalReportIdAndLecturerId(Integer finalReportId, Integer lecturerId);
}
