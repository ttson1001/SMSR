package com.example.smrsservice.repository;


import com.example.smrsservice.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    Page<Project> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Project> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    Page<Project> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable
    );
}

