package com.example.smrsservice.controller;

import com.example.smrsservice.common.ProjectStatus;
import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.project.*;
import com.example.smrsservice.entity.Project;
import com.example.smrsservice.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * ✅ API 1: Lấy tất cả projects (cho admin/dean)
     */
    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) Integer ownerId,
            @RequestParam(required = false) Integer majorId) {

        Page<ProjectResponse> result = projectService.getAllProjects(
                page, size, sortBy, sortDir, name, status, ownerId, majorId);

        return ResponseEntity.ok(result);
    }

    /**
     * ✅ API 2: Lấy các projects mà user tham gia (owner hoặc member)
     * GET /api/projects/my-projects?page=0&size=10&name=...&status=PENDING
     */
    @GetMapping("/my-projects")
    public ResponseEntity<Page<ProjectResponse>> getMyProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ProjectStatus status,
            Authentication authentication) {

        Page<ProjectResponse> result = projectService.getMyProjects(
                page, size, sortBy, sortDir, name, status, authentication);

        return ResponseEntity.ok(result);
    }

    /**
     * ✅ API 3: Tạo project mới
     */
    @PostMapping
    public ResponseEntity<ResponseDto<ProjectResponse>> createProject(
            @RequestBody ProjectCreateDto dto,
            Authentication authentication) {

        ResponseDto<ProjectResponse> response = projectService.createProject(dto, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * ✅ API 4: Import projects từ Excel (cho Dean)
     * POST /api/projects/import
     */
    @PostMapping("/import")
    public ResponseEntity<ResponseDto<List<Project>>> importProjects(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        ResponseDto<List<Project>> response = projectService.importProjectsFromExcel(file, authentication);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ API 5: Chi tiết project
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<ProjectDetailResponse>> getProjectDetail(
            @PathVariable Integer id) {

        ResponseDto<ProjectDetailResponse> response = projectService.getProjectDetail(id);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ API 6: Update status của project
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ProjectResponse> updateProjectStatus(
            @PathVariable Integer id,
            @RequestBody UpdateProjectStatusRequest request) {

        ProjectResponse response = projectService.updateProjectStatus(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ API 7: Student pick archived project
     */
    @PostMapping("/{id}/pick")
    public ResponseEntity<ResponseDto<ProjectResponse>> pickArchivedProject(
            @PathVariable Integer id,
            @RequestBody PickProjectRequest request,
            Authentication authentication) {

        ResponseDto<ProjectResponse> response = projectService.pickArchivedProject(id, request, authentication);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ API 8: Search projects
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProjectResponse>> searchProjects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<ProjectResponse> result = projectService.searchProjects(
                name, description, page, size, sortBy, sortDir);

        return ResponseEntity.ok(result);
    }
}