package com.example.smrsservice.repository;

import com.example.smrsservice.entity.PlagiarismResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlagiarismResultRepository extends JpaRepository<PlagiarismResult, Long> {

    Optional<PlagiarismResult> findTopByScanIdOrderByReceivedAtDesc(String scanId);

    @Query(value = "SELECT * FROM plagiarism_result WHERE scan_id = :scanId ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<PlagiarismResult> findByScanId(@Param("scanId") String scanId);
}