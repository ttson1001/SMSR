package com.example.smrsservice.service;

import com.example.smrsservice.common.PublicationStatus;
import com.example.smrsservice.common.PublicationType;
import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.projectpublication.CreatePublicationRequest;
import com.example.smrsservice.dto.projectpublication.ProjectPublicationDto;
import com.example.smrsservice.dto.projectpublication.UpdatePublicationRequest;
import com.example.smrsservice.entity.Account;
import com.example.smrsservice.entity.Project;
import com.example.smrsservice.entity.ProjectPublication;
import com.example.smrsservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectPublicationService {

    private final ProjectPublicationRepository publicationRepository;
    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MilestoneRepository milestoneRepository;

    /**
     * 1. CREATE - Đăng ký publication
     * Lecturer/Student của project có thể đăng ký
     */
    @Transactional
    public ResponseDto<ProjectPublicationDto> createPublication(
            CreatePublicationRequest request,
            Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            // Tìm project
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            // ========== KIỂM TRA QUYỀN ==========
            // 1. Lecturer phải là mentor của project
            // 2. Student phải là owner hoặc member của project
            boolean isAuthorized = false;

            if ("LECTURER".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
                // Check if lecturer is mentor
                isAuthorized = projectMemberRepository
                        .findByProjectId(project.getId()).stream()
                        .anyMatch(pm -> pm.getAccount().getId().equals(currentUser.getId()) &&
                                "LECTURER".equalsIgnoreCase(pm.getMemberRole()));
            } else if ("STUDENT".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
                // Check if student is owner or member
                isAuthorized = project.getOwner().getId().equals(currentUser.getId()) ||
                        projectMemberRepository.findByProjectId(project.getId()).stream()
                                .anyMatch(pm -> pm.getAccount().getId().equals(currentUser.getId()));
            }

            if (!isAuthorized) {
                return ResponseDto.fail("You are not authorized to register publication for this project");
            }

            // ========== KIỂM TRA PROJECT ĐÃ CÓ FINAL REPORT CHƯA ==========
            boolean hasFinalReport = milestoneRepository
                    .findByProjectIdAndIsFinal(project.getId(), true)
                    .isPresent();

            if (!hasFinalReport) {
                return ResponseDto.fail("Project must have a final report before registering publication");
            }

            // ========== TẠO PUBLICATION ==========
            ProjectPublication publication = ProjectPublication.builder()
                    .project(project)
                    .author(currentUser)
                    .publicationName(request.getPublicationName())
                    .publicationType(PublicationType.valueOf(request.getPublicationType().toUpperCase()))
                    .publicationLink(request.getPublicationLink())
                    .registeredDate(new Date())
                    .status(PublicationStatus.REGISTERED)
                    .notes(request.getNotes())
                    .doi(request.getDoi())
                    .isbnIssn(request.getIsbnIssn())
                    .build();

            publicationRepository.save(publication);

            System.out.println("✅ Publication registered: " + publication.getPublicationName() +
                    " for project " + project.getName());

            ProjectPublicationDto dto = toDto(publication);
            return ResponseDto.success(dto, "Publication registered successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * 2. UPDATE - Cập nhật publication
     * Chỉ author đăng ký mới được update
     */
    @Transactional
    public ResponseDto<ProjectPublicationDto> updatePublication(
            Integer publicationId,
            UpdatePublicationRequest request,
            Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            ProjectPublication publication = publicationRepository.findById(publicationId)
                    .orElseThrow(() -> new RuntimeException("Publication not found"));

            // Chỉ author đăng ký mới được update
            if (!publication.getAuthor().getId().equals(currentUser.getId())) {
                return ResponseDto.fail("Only the author who registered can update this publication");
            }

            // Update fields
            if (request.getPublicationName() != null) {
                publication.setPublicationName(request.getPublicationName());
            }
            if (request.getPublicationType() != null) {
                publication.setPublicationType(
                        PublicationType.valueOf(request.getPublicationType().toUpperCase())
                );
            }
            if (request.getPublicationLink() != null) {
                publication.setPublicationLink(request.getPublicationLink());
            }
            if (request.getStatus() != null) {
                PublicationStatus newStatus = PublicationStatus.valueOf(
                        request.getStatus().toUpperCase()
                );
                publication.setStatus(newStatus);

                // Nếu update sang PUBLISHED, set publishedDate
                if (newStatus == PublicationStatus.PUBLISHED && publication.getPublishedDate() == null) {
                    publication.setPublishedDate(new Date());
                }
            }
            if (request.getNotes() != null) {
                publication.setNotes(request.getNotes());
            }
            if (request.getDoi() != null) {
                publication.setDoi(request.getDoi());
            }
            if (request.getIsbnIssn() != null) {
                publication.setIsbnIssn(request.getIsbnIssn());
            }

            publicationRepository.save(publication);

            System.out.println("✅ Publication updated: " + publication.getPublicationName());

            ProjectPublicationDto dto = toDto(publication);
            return ResponseDto.success(dto, "Publication updated successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * 3. DELETE - Xóa publication
     * Chỉ author đăng ký mới được xóa
     */
    @Transactional
    public ResponseDto<String> deletePublication(
            Integer publicationId,
            Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            ProjectPublication publication = publicationRepository.findById(publicationId)
                    .orElseThrow(() -> new RuntimeException("Publication not found"));

            // Chỉ author đăng ký mới được xóa
            if (!publication.getAuthor().getId().equals(currentUser.getId())) {
                return ResponseDto.fail("Only the author who registered can delete this publication");
            }

            publicationRepository.delete(publication);

            System.out.println("✅ Publication deleted: " + publication.getPublicationName());

            return ResponseDto.success(null, "Publication deleted successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * 4. GET ALL - Lấy tất cả publications (for admin)
     */
    public ResponseDto<List<ProjectPublicationDto>> getAllPublications(Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            // Chỉ ADMIN mới xem được tất cả
            if (!"ADMIN".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
                return ResponseDto.fail("Only admins can view all publications");
            }

            List<ProjectPublication> publications = publicationRepository.findAll();

            List<ProjectPublicationDto> dtos = publications.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());

            return ResponseDto.success(dtos, "Found " + dtos.size() + " publication(s)");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * 5. GET BY ID - Xem chi tiết publication
     */
    public ResponseDto<ProjectPublicationDto> getPublicationById(Integer publicationId) {
        try {
            ProjectPublication publication = publicationRepository.findById(publicationId)
                    .orElseThrow(() -> new RuntimeException("Publication not found"));

            ProjectPublicationDto dto = toDto(publication);

            return ResponseDto.success(dto, "OK");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * 6. GET BY PROJECT - Lấy publications của 1 project
     */
    public ResponseDto<List<ProjectPublicationDto>> getPublicationsByProject(Integer projectId) {
        try {
            List<ProjectPublication> publications = publicationRepository
                    .findByProjectId(projectId);

            List<ProjectPublicationDto> dtos = publications.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());

            return ResponseDto.success(dtos,
                    "Found " + dtos.size() + " publication(s) for this project");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * 7. GET MY PUBLICATIONS - Lấy publications của user hiện tại
     */
    public ResponseDto<List<ProjectPublicationDto>> getMyPublications(Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            List<ProjectPublication> publications = publicationRepository
                    .findByAuthorId(currentUser.getId());

            List<ProjectPublicationDto> dtos = publications.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());

            return ResponseDto.success(dtos,
                    "Found " + dtos.size() + " publication(s)");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }
    private ProjectPublicationDto toDto(ProjectPublication pub) {

        // Build Project Info
        Project project = pub.getProject();
        ProjectPublicationDto.ProjectInfo projectInfo = null;

        if (project != null) {
            projectInfo = ProjectPublicationDto.ProjectInfo.builder()
                    .projectId(project.getId())
                    .projectName(project.getName())
                    .projectDescription(project.getDescription())
                    .projectType(project.getType())
                    .projectStatus(project.getStatus() != null
                            ? project.getStatus().toString()
                            : null)
                    .projectDueDate(project.getDueDate())
                    .projectCreateDate(project.getCreateDate())

                    // Owner info
                    .ownerId(project.getOwner() != null
                            ? project.getOwner().getId()
                            : null)
                    .ownerName(project.getOwner() != null
                            ? project.getOwner().getName()
                            : null)
                    .ownerEmail(project.getOwner() != null
                            ? project.getOwner().getEmail()
                            : null)
                    .ownerRole(project.getOwner() != null && project.getOwner().getRole() != null
                            ? project.getOwner().getRole().getRoleName()
                            : null)

                    // Major info
                    .majorId(project.getMajor() != null
                            ? project.getMajor().getId().intValue()
                            : null)
                    .majorName(project.getMajor() != null
                            ? project.getMajor().getName()
                            : null)

                    .build();
        }

        // Build Author Info
        Account author = pub.getAuthor();
        ProjectPublicationDto.AuthorInfo authorInfo = null;

        if (author != null) {
            authorInfo = ProjectPublicationDto.AuthorInfo.builder()
                    .authorId(author.getId())
                    .authorName(author.getName())
                    .authorEmail(author.getEmail())
                    .authorPhone(author.getPhone())
                    .authorAvatar(author.getAvatar())
                    .authorRole(author.getRole() != null
                            ? author.getRole().getRoleName()
                            : null)
                    .authorAge(author.getAge())
                    .authorStatus(author.getStatus() != null
                            ? author.getStatus().toString()
                            : null)
                    .build();
        }

        // Build Main DTO
        return ProjectPublicationDto.builder()  // ← RETURN ở đây!
                .id(pub.getId())
                .status(pub.getStatus() != null
                        ? pub.getStatus().getJsonName()
                        : null)
                .publicationName(pub.getPublicationName())
                .publicationType(pub.getPublicationType() != null
                        ? pub.getPublicationType().getJsonName()
                        : null)
                .publicationLink(pub.getPublicationLink())
                .registeredDate(pub.getRegisteredDate())
                .publishedDate(pub.getPublishedDate())
                .notes(pub.getNotes())
                .doi(pub.getDoi())
                .isbnIssn(pub.getIsbnIssn())
                .createdAt(pub.getCreatedAt())
                .updatedAt(pub.getUpdatedAt())

                // Full DTOs
                .project(projectInfo)
                .author(authorInfo)

                .build();
    }

    // ==================== HELPER METHODS ====================



    private Account getCurrentAccount(Authentication authentication) {
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
                    .orElseThrow(() -> new RuntimeException("Account not found"));
        }

        throw new RuntimeException("Invalid authentication principal");
    }
}
