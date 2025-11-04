package com.example.smrsservice.service;
import com.example.smrsservice.common.ProjectStatus;
import com.example.smrsservice.dto.common.ResponseDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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


    public ResponseDto<ProjectResponse> createProject(ProjectCreateDto dto, Authentication authentication) {
        try {
            Account owner = currentAccount(authentication);

            // 2. Tạo project
            Project project = new Project();
            project.setName(dto.getName());
            project.setDescription(dto.getDescription());
            project.setType(dto.getType());
            project.setDueDate(dto.getDueDate());
            project.setOwner(owner);

            // 3. Map files
            if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
                List<ProjectFile> files = dto.getFiles().stream()
                        .map(f -> {
                            ProjectFile file = new ProjectFile();
                            file.setFilePath(f.getFilePath());
                            file.setType(f.getType());
                            file.setProject(project);
                            return file;
                        })
                        .collect(Collectors.toList());
                project.setFiles(files);
            }

            // 4. Map images
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                List<ProjectImage> images = dto.getImages().stream()
                        .map(i -> {
                            ProjectImage image = new ProjectImage();
                            image.setUrl(i.getUrl());
                            image.setProject(project);
                            return image;
                        })
                        .collect(Collectors.toList());
                project.setImages(images);
            }

            // 5. Lưu
            projectRepository.save(project);

            // 6. Response
            ProjectResponse res = ProjectResponse.builder()
                    .id(project.getId())
                    .name(project.getName())
                    .description(project.getDescription())
                    .type(project.getType())
                    .dueDate(project.getDueDate())
                    .ownerId(owner.getId())
                    .ownerName(owner.getName())
                    .build();

            return ResponseDto.success(res, "Project created successfully");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
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

    private Account currentAccount(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Account) {
            return (Account) principal;
        }

        if (principal instanceof String) {
            String email = (String) principal;
            return accountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Account not found with email: " + email));
        }

        throw new RuntimeException("Invalid authentication principal type");
    }



}



