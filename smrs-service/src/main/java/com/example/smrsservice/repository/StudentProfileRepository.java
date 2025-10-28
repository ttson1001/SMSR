package com.example.smrsservice.repository;

import com.example.smrsservice.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile,Integer> {
    Optional<StudentProfile> findByAccount_Id(Integer accountId);
}
