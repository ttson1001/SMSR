package com.example.smrsservice.repository;

import com.example.smrsservice.entity.ProjectScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProjectScoreRepository extends JpaRepository<ProjectScore, Integer> {
    List<ProjectScore> findByProjectId(Integer projectId);

    List<ProjectScore> findByFinalMilestoneId(Integer milestoneId);

    @Query("SELECT AVG(ps.finalScore) FROM ProjectScore ps WHERE ps.project.id = :projectId")
    Double getAverageScoreByProjectId(@Param("projectId") Integer projectId);

    @Query("SELECT AVG(ps.finalScore) FROM ProjectScore ps WHERE ps.finalMilestone.id = :finalReportId")
    Double getAverageScoreByFinalReportId(@Param("finalReportId") Integer finalReportId);

    boolean existsByFinalMilestoneIdAndLecturerId(Integer milestoneId, Integer lecturerId);

    @Query("SELECT AVG(ps.finalScore) FROM ProjectScore ps WHERE ps.project.owner.id = :studentId")
    Double getAverageScoreByStudentId(@Param("studentId") Integer studentId);

    /**
     * Find scores by lecturer
     */
    @Query("SELECT ps FROM ProjectScore ps WHERE ps.lecturer.id = :lecturerId ORDER BY ps.scoreDate DESC")
    List<ProjectScore> findByLecturerId(@Param("lecturerId") Integer lecturerId);

}
