package com.example.smrsservice.service;

import com.example.smrsservice.dto.task.CreateTaskRequest;
import com.example.smrsservice.dto.task.TaskResponse;
import com.example.smrsservice.entity.Account;
import com.example.smrsservice.entity.Milestone;
import com.example.smrsservice.entity.Task;
import com.example.smrsservice.repository.AccountRepository;
import com.example.smrsservice.repository.MilestoneRepository;
import com.example.smrsservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    private final AccountRepository accountRepository;
    private final MilestoneRepository milestoneRepository;
//
//    public TaskResponse createTask(CreateTaskRequest request ) {
//        Task task = new Task();
//        task.setName(request.getName());
//        task.setDescription(request.getDescription());
//
//        Account accountCreator = accountRepository.findById(request.getCreatedById())
//                .orElseThrow(() -> new RuntimeException("Account not found"));
//        task.setCreatedBy(accountCreator);
//
//        if (request.getAssignedToId() != null) {
//            Account assignee = accountRepository.findById(request.getAssignedToId())
//                    .orElseThrow(() -> new RuntimeException("assignedTo not found"));
//            task.setAssignedTo(assignee);
//        }
//
//        if (request.getMilestoneId() != null) {
//            Milestone ms = milestoneRepository.findById(request.getMilestoneId())
//                    .orElseThrow(() -> new RuntimeException("milestone not found"));
//            task.setMilestone(ms);
//        }
//
//        task.setStartDate(request.getStartDate() != null ? request.getStartDate() : new Date());
//        task.setDeadline(request.getDeadline());
//        task.setProgressPercent(request.getProgressPercent() != null ? request.getProgressPercent() : 0.0);
//        task.setStatus(request.getStatus() != null ? request.getStatus() : "Pending");
//
//        task = taskRepository.save(task);
//
//        TaskResponse resp = buildResponse(task);
//        return buildResponse(task);
//
//    }
}
