package com.example.smrsservice.repository;

import com.example.smrsservice.entity.LecturerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LecturerProfileRepository extends JpaRepository<LecturerProfile, Integer> {
    Optional<LecturerProfile> findByAccountId(Integer accountId);

    List<LecturerProfile> findByMajorId(Integer majorId);

    int countByMajorId(Integer majorId);
}