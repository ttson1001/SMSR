package com.example.smrsservice.service;

import com.example.smrsservice.common.ProjectStatus;
import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.project.*;
import com.example.smrsservice.dto.score.ProjectScoreResponseDto;
import com.example.smrsservice.entity.*;
import com.example.smrsservice.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MailService mailService;
    private final MajorRepository majorRepository;
    private final ProjectScoreRepository projectScoreRepository;

    private final CouncilMemberRepository councilMemberRepository;
    private final ProjectCouncilRepository projectCouncilRepository;
    private final MilestoneRepository milestoneRepository;


    private static final int MAX_STUDENTS_PER_PROJECT = 5;

    public ProjectResponse updateProjectStatus(Integer projectId, UpdateProjectStatusRequest req) {
        if (req == null || req.getStatus() == null) {
            throw new IllegalArgumentException("Status is required");
        }
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        ProjectStatus oldS = p.getStatus() == null ? ProjectStatus.PENDING : p.getStatus();
        ProjectStatus newS = req.getStatus();

        if (!oldS.canTransitionTo(newS)) {
            throw new IllegalStateException("Không thể chuyển từ " + oldS.getJsonName() + " -> " + newS.getJsonName());
        }

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

        return toResponse(p);
    }

    public Page<ProjectResponse> getAllProjects(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String name,
            ProjectStatus status,
            Integer ownerId,
            Integer majorId,
            Boolean isMine,
            Authentication authentication) {

        Set<String> allowed = Set.of("id", "name", "type", "dueDate", "description", "createDate");
        String by = allowed.contains(sortBy) ? sortBy : "id";

        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(by).descending()
                : Sort.by(by).ascending();

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sort);

        Page<Project> result;

        if (Boolean.TRUE.equals(isMine)) {
            Account currentUser = currentAccount(authentication);
            Set<Integer> projectIds = getMyProjectIds(currentUser.getId());

            if (projectIds.isEmpty()) {
                return Page.empty(pageable);
            }

            result = projectRepository.findAll(
                    buildMyProjectsSpecification(projectIds, name, status),
                    pageable
            );
        } else {
            boolean hasName = StringUtils.hasText(name);
            boolean hasStatus = (status != null);
            boolean hasOwner = (ownerId != null);
            boolean hasMajor = (majorId != null);

            if (!hasName && !hasStatus && !hasOwner && !hasMajor) {
                result = projectRepository.findAll(pageable);
            } else {
                result = projectRepository.findAll(
                        buildSpecification(name, status, ownerId, majorId),
                        pageable
                );
            }
        }

        return result.map(this::toResponse);
    }

    private Set<Integer> getMyProjectIds(Integer userId) {
        Set<Integer> projectIds = new HashSet<>();

        List<Project> ownedProjects = projectRepository.findByOwnerId(userId);
        projectIds.addAll(ownedProjects.stream()
                .map(Project::getId)
                .collect(Collectors.toSet()));

        List<ProjectMember> memberProjects = projectMemberRepository.findByAccountId(userId);
        projectIds.addAll(memberProjects.stream()
                .map(pm -> pm.getProject().getId())
                .collect(Collectors.toSet()));

        return projectIds;
    }

    private Specification<Project> buildMyProjectsSpecification(
            Set<Integer> projectIds,
            String name,
            ProjectStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(root.get("id").in(projectIds));

            if (StringUtils.hasText(name)) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%"
                        )
                );
            }

            if (status != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("status"), status)
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Project> buildSpecification(
            String name,
            ProjectStatus status,
            Integer ownerId, Integer majorId
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%"
                        )
                );
            }

            if (status != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("status"), status)
                );
            }

            if (ownerId != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("owner").get("id"), ownerId)
                );
            }
            if (majorId != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("major").get("id"), majorId)
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public ResponseDto<ProjectResponse> createProject(ProjectCreateDto dto, Authentication authentication) {
        try {
            Account owner = currentAccount(authentication);

            if (owner.getRole() == null) {
                return ResponseDto.fail("User role not found");
            }

            String roleName = owner.getRole().getRoleName();
            if (!"LECTURER".equalsIgnoreCase(roleName) && !"STUDENT".equalsIgnoreCase(roleName)) {
                return ResponseDto.fail("Only lecturers and students can create projects");
            }

            Project project = new Project();
            project.setName(dto.getName());
            project.setDescription(dto.getDescription());
            project.setType(dto.getType());
            project.setDueDate(dto.getDueDate());
            project.setOwner(owner);
            project.setStatus(ProjectStatus.PENDING);

            if (dto.getMajorId() != null) {
                Major major = majorRepository.findById(Long.valueOf(dto.getMajorId()))
                        .orElseThrow(() -> new RuntimeException("Major not found"));
                project.setMajor(major);
            }

            if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
                for (ProjectCreateDto.FileDto f : dto.getFiles()) {
                    ProjectFile file = new ProjectFile();
                    file.setFilePath(f.getFilePath());
                    file.setType(f.getType());
                    file.setProject(project);
                    project.getFiles().add(file);
                }
            }

            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                for (ProjectCreateDto.ImageDto i : dto.getImages()) {
                    ProjectImage image = new ProjectImage();
                    image.setUrl(i.getUrl());
                    image.setProject(project);
                    project.getImages().add(image);
                }
            }

            projectRepository.save(project);

            if (dto.getInvitedEmails() != null && !dto.getInvitedEmails().isEmpty()) {
                inviteMembers(project, dto.getInvitedEmails(), owner);
            }

            ProjectResponse res = toResponse(project);

            return ResponseDto.success(res, "Project created successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    private void inviteMembers(Project project, List<String> emails, Account owner) {
        if (emails == null || emails.isEmpty()) {
            return;
        }

        int lecturerCount = 0;
        int studentCount = 0;

        long currentLecturers = projectMemberRepository.countByProjectIdAndMemberRoleAndStatus(
                project.getId(), "LECTURER", "Approved");
        long currentStudents = projectMemberRepository.countByProjectIdAndMemberRoleAndStatus(
                project.getId(), "STUDENT", "Approved");

        for (String email : emails) {
            try {
                Account invitedAccount = accountRepository.findByEmail(email.trim())
                        .orElse(null);

                if (invitedAccount == null) {
                    System.out.println("Account not found: " + email);
                    continue;
                }

                if (invitedAccount.getId().equals(owner.getId())) {
                    System.out.println("Cannot invite owner: " + email);
                    continue;
                }

                if (invitedAccount.getRole() == null) {
                    System.out.println("Account has no role: " + email);
                    continue;
                }

                boolean alreadyInvited = projectMemberRepository
                        .existsByProjectIdAndAccountId(project.getId(), invitedAccount.getId());

                if (alreadyInvited) {
                    System.out.println("Already invited: " + email);
                    continue;
                }

                String roleName = invitedAccount.getRole().getRoleName();

                if ("LECTURER".equalsIgnoreCase(roleName)) {
                    if (currentLecturers > 0 || lecturerCount > 0) {
                        System.out.println("Project already has a lecturer mentor: " + email);
                        continue;
                    }
                    lecturerCount++;

                } else if ("STUDENT".equalsIgnoreCase(roleName)) {
                    if (currentStudents + studentCount >= MAX_STUDENTS_PER_PROJECT) {
                        System.out.println("Maximum students reached: " + email);
                        continue;
                    }
                    studentCount++;

                } else {
                    System.out.println("Invalid role: " + email);
                    continue;
                }

                ProjectMember member = new ProjectMember();
                member.setProject(project);
                member.setAccount(invitedAccount);
                member.setStatus("Pending");
                member.setMemberRole(roleName.toUpperCase());
                projectMemberRepository.save(member);

                String invitationToken = generateInvitationToken(member.getId());

                try {
                    mailService.sendProjectInvitation(
                            invitedAccount.getEmail(),
                            invitedAccount.getName(),
                            project.getName(),
                            owner.getName(),
                            roleName.toUpperCase(),
                            member.getId(),
                            invitationToken
                    );
                    System.out.println("✅ Successfully invited: " + email);
                } catch (Exception emailEx) {
                    System.err.println("Failed to send email to " + email + ": " + emailEx.getMessage());
                }

            } catch (Exception e) {
                System.err.println("Error inviting " + email + ": " + e.getMessage());
            }
        }
    }

    private String generateInvitationToken(Integer invitationId) {
        String secretKey = "smrs-invitation-secret-key-2025";
        long timestamp = System.currentTimeMillis();
        String data = invitationId + ":" + secretKey + ":" + timestamp;

        return java.util.Base64.getEncoder()
                .encodeToString(data.getBytes());
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
        // ========== Lấy danh sách members ==========
        List<ProjectMember> allMembers = projectMemberRepository.findByProjectId(p.getId());

        // ========== Tìm lecturer mentor ==========
        Optional<ProjectMember> lecturerMember = allMembers.stream()
                .filter(pm -> "LECTURER".equalsIgnoreCase(pm.getMemberRole()))
                .findFirst();

        ProjectResponse.MentorInfo mentor = null;
        if (lecturerMember.isPresent()) {
            ProjectMember lm = lecturerMember.get();
            mentor = ProjectResponse.MentorInfo.builder()
                    .projectMemberId(lm.getId())
                    .accountId(lm.getAccount().getId())
                    .name(lm.getAccount().getName())
                    .email(lm.getAccount().getEmail())
                    .status(lm.getStatus())
                    .build();
        }

        // ========== Lấy danh sách students ==========
        List<ProjectResponse.StudentInfo> students = allMembers.stream()
                .filter(pm -> "STUDENT".equalsIgnoreCase(pm.getMemberRole()))
                .map(pm -> ProjectResponse.StudentInfo.builder()
                        .projectMemberId(pm.getId())
                        .accountId(pm.getAccount().getId())
                        .name(pm.getAccount().getName())
                        .email(pm.getAccount().getEmail())
                        .status(pm.getStatus())
                        .build())
                .collect(Collectors.toList());

        // ========== Build files & images như cũ ==========
        List<ProjectResponse.FileInfo> files = (p.getFiles() != null)
                ? p.getFiles().stream()
                .map(f -> ProjectResponse.FileInfo.builder()
                        .id(f.getId())
                        .fileName(f.getFilePath())
                        .fileUrl(f.getFilePath())
                        .fileType(f.getType())
                        .build())
                .collect(Collectors.toList())
                : new ArrayList<>();

        List<String> images = (p.getImages() != null)
                ? p.getImages().stream()
                .map(ProjectImage::getUrl)
                .collect(Collectors.toList())
                : new ArrayList<>();

        Instant dueDate = (p.getDueDate() != null)
                ? p.getDueDate().toInstant()
                : null;

        Instant createdAt = (p.getCreateDate() != null)
                ? p.getCreateDate().toInstant()
                : null;

        // ========== Return với mentor & students ==========
        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .type(p.getType())
                .dueDate(dueDate)

                // Owner info
                .ownerId(p.getOwner() != null ? p.getOwner().getId() : null)
                .ownerName(p.getOwner() != null ? p.getOwner().getName() : null)
                .ownerEmail(p.getOwner() != null ? p.getOwner().getEmail() : null)
                .ownerRole(p.getOwner() != null && p.getOwner().getRole() != null
                        ? p.getOwner().getRole().getRoleName() : null)

                .status(p.getStatus())
                .majorId(p.getMajor() != null ? p.getMajor().getId() : null)
                .majorName(p.getMajor() != null ? p.getMajor().getName() : null)
                .createdAt(createdAt)

                .files(files)
                .images(images)

                // ⭐⭐⭐ THÊM MỚI ⭐⭐⭐
                .mentor(mentor)
                .students(students)

                .build();
    }

    public ResponseDto<ProjectDetailResponse> getProjectDetail(Integer projectId) {
        try {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            List<ProjectMember> allMembers = projectMemberRepository.findByProjectId(projectId);

            Optional<ProjectMember> lecturerMember = allMembers.stream()
                    .filter(pm -> "LECTURER".equalsIgnoreCase(pm.getMemberRole()))
                    .findFirst();

            List<ProjectMember> studentMembers = allMembers.stream()
                    .filter(pm -> "STUDENT".equalsIgnoreCase(pm.getMemberRole()))
                    .collect(Collectors.toList());

            ProjectDetailResponse.OwnerInfo ownerInfo = ProjectDetailResponse.OwnerInfo.builder()
                    .id(project.getOwner().getId())
                    .name(project.getOwner().getName())
                    .email(project.getOwner().getEmail())
                    .role(project.getOwner().getRole() != null ?
                            project.getOwner().getRole().getRoleName() : null)
                    .build();

            ProjectDetailResponse.LecturerInfo lecturerInfo = null;
            if (lecturerMember.isPresent()) {
                ProjectMember lm = lecturerMember.get();
                lecturerInfo = ProjectDetailResponse.LecturerInfo.builder()
                        .id(lm.getId())
                        .accountId(lm.getAccount().getId())
                        .name(lm.getAccount().getName())
                        .email(lm.getAccount().getEmail())
                        .status(lm.getStatus())
                        .build();
            }

            List<ProjectDetailResponse.MemberInfo> membersInfo = studentMembers.stream()
                    .map(pm -> ProjectDetailResponse.MemberInfo.builder()
                            .id(pm.getId())
                            .accountId(pm.getAccount().getId())
                            .name(pm.getAccount().getName())
                            .email(pm.getAccount().getEmail())
                            .role(pm.getMemberRole())
                            .status(pm.getStatus())
                            .joinedDate(null)
                            .build())
                    .collect(Collectors.toList());

            List<ProjectDetailResponse.FileInfo> filesInfo = project.getFiles().stream()
                    .map(f -> ProjectDetailResponse.FileInfo.builder()
                            .id(f.getId())
                            .filePath(f.getFilePath())
                            .type(f.getType())
                            .build())
                    .collect(Collectors.toList());

            List<ProjectDetailResponse.ImageInfo> imagesInfo = project.getImages().stream()
                    .map(i -> ProjectDetailResponse.ImageInfo.builder()
                            .id(i.getId())
                            .url(i.getUrl())
                            .build())
                    .collect(Collectors.toList());

            long approvedCount = studentMembers.stream()
                    .filter(pm -> "Approved".equals(pm.getStatus()))
                    .count();
            long pendingCount = studentMembers.stream()
                    .filter(pm -> "Pending".equals(pm.getStatus()))
                    .count();

            Double avgScore = projectScoreRepository.getAverageScoreByProjectId(projectId);
            Integer totalScores = projectScoreRepository.findByProjectId(projectId).size();

            ProjectDetailResponse.Statistics statistics = ProjectDetailResponse.Statistics.builder()
                    .totalMembers(studentMembers.size())
                    .approvedMembers((int) approvedCount)
                    .pendingMembers((int) pendingCount)
                    .totalStudents(studentMembers.size())
                    .totalFiles(project.getFiles() != null ? project.getFiles().size() : 0)
                    .totalImages(project.getImages() != null ? project.getImages().size() : 0)
                    .hasLecturer(lecturerMember.isPresent() &&
                            "Approved".equals(lecturerMember.get().getStatus()))
                    .build();

            ProjectDetailResponse response = ProjectDetailResponse.builder()
                    .id(project.getId())
                    .name(project.getName())
                    .description(project.getDescription())
                    .type(project.getType())
                    .status(project.getStatus())
                    .createDate(project.getCreateDate())
                    .dueDate(project.getDueDate())
                    .owner(ownerInfo)
                    .lecturer(lecturerInfo)
                    .members(membersInfo)
                    .files(filesInfo)
                    .images(imagesInfo)
                    .statistics(statistics)
                    .averageScore(avgScore != null ? avgScore : 0.0)
                    .totalScores(totalScores)
                    .build();

            return ResponseDto.success(response, "Get project detail successfully");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<List<ProjectReviewDto>> getProjectsToReview(Authentication authentication) {
        try {
            Account lecturer = getCurrentAccount(authentication);

            if (!"LECTURER".equalsIgnoreCase(lecturer.getRole().getRoleName())) {
                return ResponseDto.fail("Only lecturers can access this endpoint");
            }

            List<CouncilMember> councilMembers = councilMemberRepository.findByLecturerId(lecturer.getId());

            if (councilMembers.isEmpty()) {
                return ResponseDto.success(new ArrayList<>(), "You are not assigned to any council");
            }

            List<Integer> councilIds = councilMembers.stream()
                    .map(cm -> cm.getCouncil().getId())
                    .collect(Collectors.toList());

            List<ProjectCouncil> projectCouncils = projectCouncilRepository.findAll().stream()
                    .filter(pc -> councilIds.contains(pc.getCouncil().getId()))
                    .collect(Collectors.toList());

            if (projectCouncils.isEmpty()) {
                return ResponseDto.success(new ArrayList<>(), "No projects assigned to your councils");
            }

            List<ProjectReviewDto> result = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (ProjectCouncil pc : projectCouncils) {
                Project project = pc.getProject();
                Council council = pc.getCouncil();

                Optional<Milestone> finalMilestoneOpt =
                        milestoneRepository.findByProjectIdAndIsFinal(project.getId(), true);

                if (!finalMilestoneOpt.isPresent()) {
                    continue;
                }

                Milestone finalMilestone = finalMilestoneOpt.get();

                List<ProjectScore> myScores = projectScoreRepository
                        .findByFinalMilestoneId(finalMilestone.getId()).stream()
                        .filter(score -> score.getLecturer().getId().equals(lecturer.getId()))
                        .collect(Collectors.toList());

                ProjectScore myScore = myScores.isEmpty() ? null : myScores.get(0);

                Double avgScore =
                        projectScoreRepository.getAverageScoreByFinalReportId(finalMilestone.getId());

                List<ProjectScore> allScores =
                        projectScoreRepository.findByFinalMilestoneId(finalMilestone.getId());

                List<CouncilMember> councilMembersList =
                        councilMemberRepository.findByCouncilId(council.getId());

                List<ProjectMember> projectMembers =
                        projectMemberRepository.findByProjectId(project.getId());

                long totalStudents = projectMembers.stream()
                        .filter(pm -> "STUDENT".equalsIgnoreCase(pm.getMemberRole()))
                        .count();

                boolean hasLecturer = projectMembers.stream()
                        .anyMatch(pm -> "LECTURER".equalsIgnoreCase(pm.getMemberRole())
                                && "Approved".equals(pm.getStatus()));

                ProjectReviewDto dto = ProjectReviewDto.builder()
                        .projectId(project.getId())
                        .projectName(project.getName())
                        .projectDescription(project.getDescription())
                        .projectType(project.getType())
                        .projectStatus(project.getStatus().toString())
                        .projectCreateDate(project.getCreateDate())
                        .projectDueDate(project.getDueDate())

                        .finalMilestoneId(finalMilestone.getId())
                        .reportTitle("Final Milestone Report")
                        .reportDescription(finalMilestone.getReportComment())
                        .reportFilePath(finalMilestone.getReportUrl())
                        .reportSubmissionDate(finalMilestone.getReportSubmittedAt() != null
                                ? sdf.format(finalMilestone.getReportSubmittedAt())
                                : null)
                        .reportSubmittedBy(finalMilestone.getReportSubmittedBy() != null
                                ? finalMilestone.getReportSubmittedBy().getName()
                                : null)

                        .councilId(council.getId())
                        .councilName(council.getCouncilName())
                        .councilCode(council.getCouncilCode())
                        .councilDepartment(council.getDepartment())

                        .hasScored(myScore != null)
                        .myScoreId(myScore != null ? myScore.getId() : null)
                        .myFinalScore(myScore != null ? myScore.getFinalScore() : null)
                        .currentAverage(avgScore != null ? avgScore : 0.0)
                        .totalScores(allScores.size())
                        .totalCouncilMembers(councilMembersList.size())

                        .ownerId(project.getOwner().getId())
                        .ownerName(project.getOwner().getName())
                        .ownerEmail(project.getOwner().getEmail())
                        .ownerRole(project.getOwner().getRole() != null
                                ? project.getOwner().getRole().getRoleName()
                                : null)

                        .totalMembers(projectMembers.size())
                        .totalStudents((int) totalStudents)
                        .hasLecturer(hasLecturer)

                        .build();

                result.add(dto);
            }

            return ResponseDto.success(result, "Found " + result.size() + " projects to review");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<ProjectScoreResponseDto> getMyScoreForProject(
            Integer projectId, Authentication authentication) {
        try {
            Account lecturer = getCurrentAccount(authentication);

            // Lấy final milestone của project
            Milestone finalMilestone = milestoneRepository
                    .findByProjectIdAndIsFinal(projectId, true)
                    .orElseThrow(() -> new RuntimeException("Final milestone not found for this project"));

            // Lấy điểm mà giảng viên này đã chấm cho final milestone đó
            List<ProjectScore> scores = projectScoreRepository
                    .findByFinalMilestoneId(finalMilestone.getId()).stream()
                    .filter(score -> score.getLecturer().getId().equals(lecturer.getId()))
                    .collect(Collectors.toList());

            if (scores.isEmpty()) {
                return ResponseDto.fail("You have not scored this project yet");
            }

            ProjectScore myScore = scores.get(0);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            ProjectScoreResponseDto dto = ProjectScoreResponseDto.builder()
                    .id(myScore.getId())
                    .projectId(myScore.getProject().getId())
                    .projectName(myScore.getProject().getName())

                    // final milestone
                    .finalMilestoneId(finalMilestone.getId())
                    .reportFilePath(finalMilestone.getReportUrl())
                    .reportSubmissionDate(finalMilestone.getReportSubmittedAt() != null
                            ? sdf.format(finalMilestone.getReportSubmittedAt())
                            : null)

                    // lecturer
                    .lecturerId(myScore.getLecturer().getId())
                    .lecturerName(myScore.getLecturer().getName())

                    // scores
                    .criteria1Score(myScore.getCriteria1Score())
                    .criteria2Score(myScore.getCriteria2Score())
                    .criteria3Score(myScore.getCriteria3Score())
                    .criteria4Score(myScore.getCriteria4Score())
                    .criteria5Score(myScore.getCriteria5Score())
                    .criteria6Score(myScore.getCriteria6Score())
                    .bonusScore1(myScore.getBonusScore1())
                    .bonusScore2(myScore.getBonusScore2())

                    .totalScore(myScore.getTotalScore())
                    .finalScore(myScore.getFinalScore())
                    .comment(myScore.getComment())

                    .scoreDate(myScore.getScoreDate() != null
                            ? sdf.format(myScore.getScoreDate())
                            : null)
                    .build();

            return ResponseDto.success(dto, "OK");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }


    @Transactional
    public ResponseDto<ProjectResponse> pickArchivedProject(
            Integer projectId,
            PickProjectRequest request,
            Authentication authentication) {
        try {
            Account student = currentAccount(authentication);

            if (student.getRole() == null || !"STUDENT".equalsIgnoreCase(student.getRole().getRoleName())) {
                return ResponseDto.fail("Only students can pick archived projects");
            }

            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            if (project.getStatus() != ProjectStatus.ARCHIVED) {
                return ResponseDto.fail("This project is not available for picking");
            }

            project.setOwner(student);
            project.setStatus(ProjectStatus.PENDING);

            if (request.getDescription() != null) {
                project.setDescription(request.getDescription());
            }

            if (request.getFiles() != null && !request.getFiles().isEmpty()) {
                project.getFiles().clear();
                for (PickProjectRequest.FileDto f : request.getFiles()) {
                    ProjectFile file = new ProjectFile();
                    file.setFilePath(f.getFilePath());
                    file.setType(f.getType());
                    file.setProject(project);
                    project.getFiles().add(file);
                }
            }

            if (request.getImages() != null && !request.getImages().isEmpty()) {
                project.getImages().clear();
                for (PickProjectRequest.ImageDto i : request.getImages()) {
                    ProjectImage image = new ProjectImage();
                    image.setUrl(i.getUrl());
                    image.setProject(project);
                    project.getImages().add(image);
                }
            }

            projectRepository.save(project);

            return ResponseDto.success(toResponse(project), "Project picked successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    @Transactional
    public ResponseDto<List<Project>> importProjectsFromExcel(MultipartFile file, Authentication authentication) {
        List<Project> projects = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            Account currentUser = currentAccount(authentication);

            Map<String, Integer> headerMap = new HashMap<>();
            if (rows.hasNext()) {
                Row headerRow = rows.next();
                for (Cell cell : headerRow) {
                    String columnName = cell.getStringCellValue().trim().toLowerCase();
                    headerMap.put(columnName, cell.getColumnIndex());
                }
            }

            if (!headerMap.containsKey("name")) {
                throw new RuntimeException("Missing required column: name");
            }

            while (rows.hasNext()) {
                Row row = rows.next();

                String name = getCellValue(row.getCell(headerMap.get("name")));
                if (name == null || name.isBlank()) continue;

                Project project = new Project();
                project.setName(name);
                project.setOwner(currentUser);
                project.setStatus(ProjectStatus.PENDING);
                project.setCreateDate(new Date());

                if (headerMap.containsKey("description")) {
                    project.setDescription(getCellValue(row.getCell(headerMap.get("description"))));
                }

                if (headerMap.containsKey("type")) {
                    project.setType(getCellValue(row.getCell(headerMap.get("type"))));
                }

                if (headerMap.containsKey("duedate")) {
                    Cell dueDateCell = row.getCell(headerMap.get("duedate"));
                    if (dueDateCell != null && dueDateCell.getCellType() == CellType.NUMERIC) {
                        Date dueDate = dueDateCell.getDateCellValue();
                        project.setDueDate(dueDate);
                    } else {
                        String dueDateStr = getCellValue(dueDateCell);
                        if (!dueDateStr.isEmpty()) {
                            try {
                                LocalDateTime ldt = LocalDateTime.parse(dueDateStr + "T00:00:00");
                                project.setDueDate(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
                            } catch (Exception e) {
                                System.err.println("Invalid date format: " + dueDateStr);
                            }
                        }
                    }
                }

                if (headerMap.containsKey("major")) {
                    String majorName = getCellValue(row.getCell(headerMap.get("major")));
                    if (!majorName.isEmpty()) {
                        Optional<Major> majorOpt = majorRepository.findByName(majorName);
                        majorOpt.ifPresent(project::setMajor);
                    }
                }

                if (headerMap.containsKey("status")) {
                    String statusStr = getCellValue(row.getCell(headerMap.get("status"))).toUpperCase();
                    try {
                        ProjectStatus status = ProjectStatus.valueOf(statusStr);
                        project.setStatus(status);
                    } catch (Exception e) {
                        project.setStatus(ProjectStatus.PENDING);
                    }
                }

                projects.add(project);
            }

            projectRepository.saveAll(projects);

            return ResponseDto.success(projects, "Imported " + projects.size() + " projects successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail("Lỗi khi đọc file Excel: " + e.getMessage());
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC)
            return String.valueOf((int) cell.getNumericCellValue());
        return "";
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

    private Account getCurrentAccount(Authentication authentication) {
        return currentAccount(authentication);
    }


    /**
     * API cho user (cả student và lecturer) xem final report của các project mà họ tham gia
     * Bao gồm cả owner và member
     */
    public ResponseDto<List<MyProjectReviewDto>> getMyProjectsWithFinalReport(Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            // Lấy tất cả projects mà user này là owner hoặc là member
            Set<Integer> projectIds = getMyProjectIds(currentUser.getId());

            if (projectIds.isEmpty()) {
                return ResponseDto.success(new ArrayList<>(), "You are not a member of any project");
            }

            List<Project> myProjects = projectRepository.findAllById(projectIds);

            List<MyProjectReviewDto> result = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (Project project : myProjects) {
                // ========== Chỉ lấy những project có final milestone ==========
                Optional<Milestone> finalMilestoneOpt =
                        milestoneRepository.findByProjectIdAndIsFinal(project.getId(), true);

                if (!finalMilestoneOpt.isPresent()) {
                    continue; // Skip nếu chưa có final milestone
                }

                Milestone finalMilestone = finalMilestoneOpt.get();

                // ========== Lấy thông tin council nếu có ==========
                Optional<ProjectCouncil> projectCouncilOpt = projectCouncilRepository
                        .findAll().stream()
                        .filter(pc -> pc.getProject().getId().equals(project.getId()))
                        .findFirst();

                Council council = projectCouncilOpt.isPresent()
                        ? projectCouncilOpt.get().getCouncil()
                        : null;

                // ========== Lấy điểm trung bình và tổng số điểm ==========
                Double avgScore = projectScoreRepository
                        .getAverageScoreByFinalReportId(finalMilestone.getId());

                List<ProjectScore> allScores = projectScoreRepository
                        .findByFinalMilestoneId(finalMilestone.getId());

                // ========== Lấy thông tin members ==========
                List<ProjectMember> projectMembers = projectMemberRepository
                        .findByProjectId(project.getId());

                // Lấy thông tin lecturer mentor
                Optional<ProjectMember> lecturerMember = projectMembers.stream()
                        .filter(pm -> "LECTURER".equalsIgnoreCase(pm.getMemberRole()))
                        .findFirst();

                List<ProjectMember> studentMembers = projectMembers.stream()
                        .filter(pm -> "STUDENT".equalsIgnoreCase(pm.getMemberRole()))
                        .collect(Collectors.toList());

                long approvedStudents = studentMembers.stream()
                        .filter(pm -> "Approved".equals(pm.getStatus()))
                        .count();

                long pendingStudents = studentMembers.stream()
                        .filter(pm -> "Pending".equals(pm.getStatus()))
                        .count();

                // ========== Xác định role của user hiện tại trong project ==========
                String myRoleInProject = "MEMBER";
                Integer myMemberId = null;

                if (project.getOwner().getId().equals(currentUser.getId())) {
                    myRoleInProject = "OWNER";
                } else {
                    // Tìm trong danh sách members
                    Optional<ProjectMember> myMember = projectMembers.stream()
                            .filter(pm -> pm.getAccount().getId().equals(currentUser.getId()))
                            .findFirst();
                    if (myMember.isPresent()) {
                        myMemberId = myMember.get().getId();
                    }
                }

                // ========== Lấy danh sách giảng viên đã chấm điểm ==========
                List<MyProjectReviewDto.LecturerScoreInfo> lecturerScores = new ArrayList<>();
                for (ProjectScore score : allScores) {
                    MyProjectReviewDto.LecturerScoreInfo scoreInfo =
                            MyProjectReviewDto.LecturerScoreInfo.builder()
                                    .scoreId(score.getId())
                                    .lecturerId(score.getLecturer().getId())
                                    .lecturerName(score.getLecturer().getName())
                                    .lecturerEmail(score.getLecturer().getEmail())

                                    .criteria1Score(score.getCriteria1Score())
                                    .criteria2Score(score.getCriteria2Score())
                                    .criteria3Score(score.getCriteria3Score())
                                    .criteria4Score(score.getCriteria4Score())
                                    .criteria5Score(score.getCriteria5Score())
                                    .criteria6Score(score.getCriteria6Score())
                                    .bonusScore1(score.getBonusScore1())
                                    .bonusScore2(score.getBonusScore2())

                                    .totalScore(score.getTotalScore())
                                    .finalScore(score.getFinalScore())
                                    .comment(score.getComment())
                                    .scoreDate(score.getScoreDate() != null
                                            ? sdf.format(score.getScoreDate())
                                            : null)
                                    .build();
                    lecturerScores.add(scoreInfo);
                }

                // ========== Lấy danh sách council members nếu có ==========
                List<MyProjectReviewDto.CouncilMemberInfo> councilMembersList = new ArrayList<>();
                if (council != null) {
                    List<CouncilMember> councilMembers = councilMemberRepository
                            .findByCouncilId(council.getId());

                    for (CouncilMember cm : councilMembers) {
                        // Check xem giảng viên này đã chấm chưa
                        boolean hasScored = allScores.stream()
                                .anyMatch(score -> score.getLecturer().getId().equals(cm.getLecturer().getId()));

                        MyProjectReviewDto.CouncilMemberInfo memberInfo =
                                MyProjectReviewDto.CouncilMemberInfo.builder()
                                        .councilMemberId(cm.getId())
                                        .lecturerId(cm.getLecturer().getId())
                                        .lecturerName(cm.getLecturer().getName())
                                        .lecturerEmail(cm.getLecturer().getEmail())
                                        .role(cm.getRole())
                                        .hasScored(hasScored)
                                        .build();
                        councilMembersList.add(memberInfo);
                    }
                }

                // ========== Lấy danh sách members trong project ==========
                List<MyProjectReviewDto.MemberInfo> membersList = new ArrayList<>();
                for (ProjectMember pm : projectMembers) {
                    MyProjectReviewDto.MemberInfo memberInfo =
                            MyProjectReviewDto.MemberInfo.builder()
                                    .projectMemberId(pm.getId())
                                    .accountId(pm.getAccount().getId())
                                    .name(pm.getAccount().getName())
                                    .email(pm.getAccount().getEmail())
                                    .role(pm.getMemberRole())
                                    .status(pm.getStatus())
                                    .build();
                    membersList.add(memberInfo);
                }

                // ========== Build DTO ==========
                MyProjectReviewDto dto = MyProjectReviewDto.builder()
                        // Project basic info
                        .projectId(project.getId())
                        .projectName(project.getName())
                        .projectDescription(project.getDescription())
                        .projectType(project.getType())
                        .projectStatus(project.getStatus().toString())
                        .projectCreateDate(project.getCreateDate())
                        .projectDueDate(project.getDueDate())

                        // Owner info
                        .ownerId(project.getOwner().getId())
                        .ownerName(project.getOwner().getName())
                        .ownerEmail(project.getOwner().getEmail())
                        .ownerRole(project.getOwner().getRole() != null
                                ? project.getOwner().getRole().getRoleName()
                                : null)

                        // My role in this project
                        .myRoleInProject(myRoleInProject)
                        .myMemberId(myMemberId)

                        // Final milestone info
                        .finalMilestoneId(finalMilestone.getId())
                        .reportTitle("Final Milestone Report")
                        .reportDescription(finalMilestone.getReportComment())
                        .reportFilePath(finalMilestone.getReportUrl())
                        .reportSubmissionDate(finalMilestone.getReportSubmittedAt() != null
                                ? sdf.format(finalMilestone.getReportSubmittedAt())
                                : null)
                        .reportSubmittedBy(finalMilestone.getReportSubmittedBy() != null
                                ? finalMilestone.getReportSubmittedBy().getName()
                                : null)

                        // Council info
                        .councilId(council != null ? council.getId() : null)
                        .councilName(council != null ? council.getCouncilName() : null)
                        .councilCode(council != null ? council.getCouncilCode() : null)
                        .councilDepartment(council != null ? council.getDepartment() : null)
                        .councilMembers(councilMembersList)

                        // Lecturer mentor info
                        .hasLecturerMentor(lecturerMember.isPresent() &&
                                "Approved".equals(lecturerMember.get().getStatus()))
                        .lecturerMentorId(lecturerMember.isPresent()
                                ? lecturerMember.get().getAccount().getId()
                                : null)
                        .lecturerMentorName(lecturerMember.isPresent()
                                ? lecturerMember.get().getAccount().getName()
                                : null)
                        .lecturerMentorEmail(lecturerMember.isPresent()
                                ? lecturerMember.get().getAccount().getEmail()
                                : null)
                        .lecturerMentorStatus(lecturerMember.isPresent()
                                ? lecturerMember.get().getStatus()
                                : null)

                        // Scoring info
                        .hasBeenScored(!allScores.isEmpty())
                        .averageScore(avgScore != null ? avgScore : 0.0)
                        .totalScores(allScores.size())
                        .expectedTotalScores(councilMembersList.size())
                        .lecturerScores(lecturerScores)

                        // Member statistics
                        .totalMembers(projectMembers.size())
                        .totalStudents(studentMembers.size())
                        .approvedStudents((int) approvedStudents)
                        .pendingStudents((int) pendingStudents)
                        .members(membersList)

                        .build();

                result.add(dto);
            }

            // ========== Sort by scoring status and due date ==========
            result.sort((a, b) -> {
                // Projects đã có điểm lên trước
                if (a.getHasBeenScored() != b.getHasBeenScored()) {
                    return a.getHasBeenScored() ? -1 : 1;
                }
                // Sau đó sort theo due date (gần nhất lên trước)
                if (a.getProjectDueDate() != null && b.getProjectDueDate() != null) {
                    return a.getProjectDueDate().compareTo(b.getProjectDueDate());
                }
                return 0;
            });

            return ResponseDto.success(result, "Found " + result.size() + " project(s) with final report");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail("Error: " + e.getMessage());
        }
    }
}