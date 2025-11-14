package com.example.smrsservice.controller;

import com.example.smrsservice.common.ProjectStatus;
import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.project.PickProjectRequest;
import com.example.smrsservice.dto.project.ProjectCreateDto;
import com.example.smrsservice.dto.project.ProjectDetailResponse;
import com.example.smrsservice.dto.project.ProjectResponse;
import com.example.smrsservice.dto.project.UpdateProjectStatusRequest;
import com.example.smrsservice.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PatchMapping("/{id}/status")
    public ProjectResponse updateStatus(
            @PathVariable("id") Integer id,
            @RequestBody UpdateProjectStatusRequest request
    ) {
        return projectService.updateProjectStatus(id, request);
    }

    @GetMapping
    public Page<ProjectResponse> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) Integer ownerId,
            @RequestParam(required = false) Integer majorId) {
        return projectService.getAllProjects(page, size, sortBy, sortDir, name, status, ownerId, majorId);
    }

    @PostMapping
    public ResponseEntity<ResponseDto<ProjectResponse>> createProject(
            @RequestBody ProjectCreateDto dto,
            Authentication authentication) {

        ResponseDto<ProjectResponse> response = projectService.createProject(dto, authentication);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{projectId}/pick")
    public ResponseEntity<ResponseDto<ProjectResponse>> pickArchivedProject(
            @PathVariable Integer projectId,
            @RequestBody PickProjectRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(projectService.pickArchivedProject(projectId, request, authentication));
    }

    @GetMapping("/search")
    public Page<ProjectResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return projectService.searchProjects(name, description, page, size, sortBy, sortDir);
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<ResponseDto<ProjectDetailResponse>> getProjectDetail(
            @PathVariable Integer id) {
        return ResponseEntity.ok(projectService.getProjectDetail(id));
    }
}