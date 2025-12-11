package com.example.smrsservice.repository;

import com.example.smrsservice.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MajorRepository  extends JpaRepository<Major,Integer> {
    Optional<Major> findByCode(String code);

    @Query("SELECT m FROM Major m WHERE m.isActive = true ORDER BY m.name ASC")
    List<Major> findAllActiveMajors();

    Optional<Major> findByName(String name);
}
