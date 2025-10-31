package com.example.smrsservice.repository;

import com.example.smrsservice.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    Page<Task> findByStatusIgnoreCase(String status, PageRequest pageRequest);

}
