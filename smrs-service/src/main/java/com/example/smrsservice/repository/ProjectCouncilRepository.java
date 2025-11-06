package com.example.smrsservice.repository;

import com.example.smrsservice.entity.ProjectCouncil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectCouncilRepository extends JpaRepository<ProjectCouncil,Integer> {
    Optional<ProjectCouncil> findByProjectIdAndCouncilId(Integer projectId, Integer councilId);

    List<ProjectCouncil> findByProjectId(Integer projectId);

    List<ProjectCouncil> findByCouncilId(Integer councilId);

    List<ProjectCouncil> findByDecision(String decision);

    @Query("SELECT pc FROM ProjectCouncil pc " +
            "WHERE pc.council.dean.id = :deanId AND pc.decision = 'PENDING'")
    List<ProjectCouncil> findPendingProjectsByDean(@Param("deanId") Integer deanId);
}
