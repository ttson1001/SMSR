package com.example.smrsservice.controller;

import com.example.smrsservice.dto.account.PageResponse;
import com.example.smrsservice.dto.task.CreateTaskRequest;
import com.example.smrsservice.dto.task.TaskResponse;
import com.example.smrsservice.dto.task.UpdateTaskRequest;
import com.example.smrsservice.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public TaskResponse create(@RequestBody CreateTaskRequest request) {
        return taskService.createTask(request);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Integer id,
                               @RequestBody UpdateTaskRequest request) {
        return taskService.updateTask(id, request);
    }

    @GetMapping("/{id}")
    public TaskResponse getOne(@PathVariable Integer id) {
        return taskService.getTask(id);
    }

    @GetMapping
    public PageResponse<TaskResponse> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return taskService.getTasks(page, size);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        taskService.deleteTask(id);
    }

    @PutMapping("/{taskId}/assign/{accountId}")
    public TaskResponse assignTask(@PathVariable Integer taskId,
                                   @PathVariable Integer accountId) {
        return taskService.assignTask(taskId, accountId);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<PageResponse<TaskResponse>> getTasksByProject(
            @PathVariable Integer projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId, page, size));
    }

    @GetMapping("/project/{projectId}/status/{status}")
    public ResponseEntity<PageResponse<TaskResponse>> getTasksByProjectAndStatus(
            @PathVariable Integer projectId,
            @PathVariable String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(taskService.getTasksByProjectAndStatus(projectId, status, page, size));
    }

    @GetMapping("/status/{status}")
    public PageResponse<TaskResponse> getByStatus(@PathVariable String status,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return taskService.getTasksByStatus(status, page, size);
    }
}
