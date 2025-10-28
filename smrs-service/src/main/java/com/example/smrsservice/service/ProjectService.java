package com.example.smrsservice.service;
import com.example.smrsservice.common.ProjectStatus;
import com.example.smrsservice.dto.project.ProjectCreateDto;
import com.example.smrsservice.dto.project.ProjectResponse;
import com.example.smrsservice.dto.project.UpdateProjectStatusRequest;
import com.example.smrsservice.entity.Account;
import com.example.smrsservice.entity.Project;
import com.example.smrsservice.entity.ProjectFile;
import com.example.smrsservice.entity.ProjectImage;
import com.example.smrsservice.repository.AccountRepository;
import com.example.smrsservice.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;

    public ProjectResponse updateProjectStatus(Integer projectId, UpdateProjectStatusRequest req) {
        if (req == null || req.getStatus() == null) {
            throw new IllegalArgumentException("Status is required");
        }
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        ProjectStatus oldS = p.getStatus() == null ? ProjectStatus.PENDING : p.getStatus();
        ProjectStatus newS = req.getStatus();

        // Rule chuyển trạng thái
        if (!oldS.canTransitionTo(newS)) {
            throw new IllegalStateException("Không thể chuyển từ " + oldS.getJsonName() + " -> " + newS.getJsonName());
        }

        // Kiểm tra quyền: Owner hoặc ADMIN
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth != null) ? auth.getName() : null;
        boolean isOwner = p.getOwner() != null && p.getOwner().getEmail().equalsIgnoreCase(email);
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));
        if (!isOwner && !isAdmin) {
            throw new SecurityException("Không có quyền cập nhật trạng thái dự án");
        }

        p.setStatus(newS);
        projectRepository.save(p);

        // (tuỳ chọn) nếu muốn lưu lịch sử trạng thái, insert vào bảng ProjectStatusHistory ở đây

        return toResponse(p);
    }




    // --- GET ALL (paged + sort) ---
    public Page<ProjectResponse> getAll(int page, int size, String sortBy, String sortDir) {
        // whitelist tránh client truyền linh tinh
        java.util.Set<String> allowed = java.util.Set.of("id","name","type","dueDate","description");
        String by = allowed.contains(sortBy) ? sortBy : "id";

        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(by).descending()
                : Sort.by(by).ascending();

        Pageable pageable = PageRequest.of(Math.max(page,0), Math.max(size,1), sort);
        return projectRepository.findAll(pageable).map(this::toResponse);
    }


    public void createProject(ProjectCreateDto dto) {
        Account owner = accountRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setType(dto.getType());
        project.setDueDate(dto.getDueDate());
        project.setOwner(owner);

        // Map files
        if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
            dto.getFiles().forEach(f -> {
                ProjectFile file = new ProjectFile();
                file.setFilePath(f.getFilePath());
                file.setType(f.getType());
                file.setProject(project);
                project.getFiles().add(file);
            });
        }

        // Map images
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            dto.getImages().forEach(i -> {
                ProjectImage image = new ProjectImage();
                image.setUrl(i.getUrl());
                image.setProject(project);
                project.getImages().add(image);
            });
        }

        projectRepository.save(project);
    }

    public Page<ProjectResponse> searchProjects(
            String name,
            String description,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        String n = (name != null) ? name.trim() : null;
        String d = (description != null) ? description.trim() : null;

        Sort sort = ("desc".equalsIgnoreCase(sortDir))
                ? Sort.by(sortBy == null ? "id" : sortBy).descending()
                : Sort.by(sortBy == null ? "id" : sortBy).ascending();

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sort);

        Page<Project> result;
        boolean hasName = StringUtils.hasText(n);
        boolean hasDesc = StringUtils.hasText(d);

        if (!hasName && !hasDesc) {
            result = projectRepository.findAll(pageable);
        } else if (hasName && hasDesc) {
            result = projectRepository
                    .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(n, d, pageable);
        } else if (hasName) {
            result = projectRepository.findByNameContainingIgnoreCase(n, pageable);
        } else {
            result = projectRepository.findByDescriptionContainingIgnoreCase(d, pageable);
        }

        return result.map(this::toResponse);
    }

    private ProjectResponse toResponse(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .type(p.getType())
                .dueDate(p.getDueDate())
                .ownerId(p.getOwner() != null ? p.getOwner().getId() : null)
                .ownerName(p.getOwner() != null ? p.getOwner().getName() : null)
                .build();
    }
}



