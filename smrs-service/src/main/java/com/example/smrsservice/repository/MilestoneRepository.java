package com.example.smrsservice.repository;


import com.example.smrsservice.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Integer> {
    List<Milestone> findByProject_Id(Integer projectId);
}
