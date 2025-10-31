package com.example.smrsservice.service;

import com.example.smrsservice.dto.account.PageResponse;
import com.example.smrsservice.dto.task.AccountShortDto;
import com.example.smrsservice.dto.task.CreateTaskRequest;
import com.example.smrsservice.dto.task.TaskResponse;
import com.example.smrsservice.dto.task.UpdateTaskRequest;
import com.example.smrsservice.entity.Account;
import com.example.smrsservice.entity.Milestone;
import com.example.smrsservice.entity.Task;
import com.example.smrsservice.repository.AccountRepository;
import com.example.smrsservice.repository.MilestoneRepository;
import com.example.smrsservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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


    public TaskResponse createTask(CreateTaskRequest request) {

        // lấy user đang login làm createdBy
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account createdBy = null;
        if (auth != null && auth.getName() != null) {
            createdBy = accountRepository.findByEmail(auth.getName())
                    .orElse(null);
        }

        Task task = new Task();
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setCreatedBy(createdBy);
        task.setStartDate(new Date());
        task.setDeadline(request.getDeadline());
        task.setProgressPercent(0.0);
        task.setStatus("Pending");

        if (request.getAssignedToId() != null) {
            Account assigned = accountRepository.findById(request.getAssignedToId())
                    .orElse(null);
            task.setAssignedTo(assigned);
        }

        if (request.getMilestoneId() != null) {
            Milestone milestone = milestoneRepository.findById(request.getMilestoneId())
                    .orElse(null);
            task.setMilestone(milestone);
        }

        taskRepository.save(task);

        return buildTaskResponse(task);
    }

    public TaskResponse updateTask(Integer id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (request.getName() != null) task.setName(request.getName());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getDeadline() != null) task.setDeadline(request.getDeadline());
        if (request.getProgressPercent() != null) task.setProgressPercent(request.getProgressPercent());
        if (request.getStatus() != null) task.setStatus(request.getStatus());

        if (request.getAssignedToId() != null) {
            Account assigned = accountRepository.findById(request.getAssignedToId())
                    .orElse(null);
            task.setAssignedTo(assigned);
        }

        if (request.getMilestoneId() != null) {
            Milestone milestone = milestoneRepository.findById(request.getMilestoneId())
                    .orElse(null);
            task.setMilestone(milestone);
        }

        taskRepository.save(task);

        return buildTaskResponse(task);
    }

    public TaskResponse getTask(Integer id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return buildTaskResponse(task);
    }

    // ====== LIST / PAGE ======
    public PageResponse<TaskResponse> getTasks(int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Task> taskPage = taskRepository.findAll(pageable);

        return PageResponse.<TaskResponse>builder()
                .currentPages(page)
                .pageSizes(size)
                .totalPages(taskPage.getTotalPages())
                .totalElements(taskPage.getTotalElements())
                .data(taskPage.getContent()
                        .stream()
                        .map(this::buildTaskResponse)
                        .toList())
                .build();
    }

    public void deleteTask(Integer id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Task not found");
        }
        taskRepository.deleteById(id);
    }
    private TaskResponse buildTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .createdBy(task.getCreatedBy() != null
                        ? AccountShortDto.builder()
                        .id(task.getCreatedBy().getId())
                        .name(task.getCreatedBy().getName())
                        .email(task.getCreatedBy().getEmail())
                        .build()
                        : null)
                .assignedTo(task.getAssignedTo() != null
                        ? AccountShortDto.builder()
                        .id(task.getAssignedTo().getId())
                        .name(task.getAssignedTo().getName())
                        .email(task.getAssignedTo().getEmail())
                        .build()
                        : null)
                .startDate(task.getStartDate())
                .deadline(task.getDeadline())
                .progressPercent(task.getProgressPercent())
                .status(task.getStatus())
                .milestoneId(task.getMilestone() != null ? task.getMilestone().getId() : null)
                .milestoneName(task.getMilestone() != null ? task.getMilestone().getDescription() : null)
                .build();
    }

    public TaskResponse assignTask(Integer taskId, Integer accountId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Account assignee = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        task.setAssignedTo(assignee);

        if (task.getStatus() == null || task.getStatus().equalsIgnoreCase("Pending")) {
            task.setStatus("InProgress");
        }

        taskRepository.save(task);

        return buildTaskResponse(task);
    }
    public PageResponse<TaskResponse> getTasksByStatus(String status, int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Task> taskPage = taskRepository.findByStatusIgnoreCase(status, pageable);

        return PageResponse.<TaskResponse>builder()
                .currentPages(page)
                .pageSizes(size)
                .totalPages(taskPage.getTotalPages())
                .totalElements(taskPage.getTotalElements())
                .data(taskPage.getContent()
                        .stream()
                        .map(this::buildTaskResponse)
                        .toList())
                .build();
    }

}
