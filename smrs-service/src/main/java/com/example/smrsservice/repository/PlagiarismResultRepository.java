package com.example.smrsservice.repository;

import com.example.smrsservice.entity.PlagiarismResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlagiarismResultRepository extends JpaRepository<PlagiarismResult, Long> {

    Optional<PlagiarismResult> findTopByScanIdOrderByReceivedAtDesc(String scanId);
    Optional<PlagiarismResult> findByScanId(String scanId);
}