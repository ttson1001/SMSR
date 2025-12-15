package com.example.smrsservice.service;

import com.example.smrsservice.common.DecisionStatus;
import com.example.smrsservice.common.ProjectStatus;
import com.example.smrsservice.dto.account.PageResponse;
import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.concil.*;
import com.example.smrsservice.entity.*;
import com.example.smrsservice.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouncilService {
    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final CouncilManagerProfileRepository councilProfileRepository;
    private final AccountRepository accountRepository;
    private final ProjectRepository projectRepository;
    private final ProjectCouncilRepository projectCouncilRepository;
    private final LecturerProfileRepository lecturerProfileRepository;
    private final MilestoneRepository milestoneRepository;


    /**
     * 1. Trưởng khoa tạo hội đồng
     */
    @Transactional
    public ResponseDto<CouncilResponse> createCouncil(
            CreateCouncilRequest request,
            Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            // Kiểm tra role DEAN
            if (currentUser.getRole() == null ||
                    !"DEAN".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
                return ResponseDto.fail("Only deans can create councils");
            }

            // Lấy dean profile
            CouncilManagerProfile deanProfile = councilProfileRepository
                    .findByAccountId(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Dean profile not found"));

            // Kiểm tra council code đã tồn tại chưa
            if (councilRepository.findByCouncilCode(request.getCouncilCode()).isPresent()) {
                return ResponseDto.fail("Council code already exists");
            }

            // Tạo council
            Council council = new Council();
            council.setCouncilCode(request.getCouncilCode());
            council.setCouncilName(request.getCouncilName());
            council.setDepartment(request.getDepartment());
            council.setDescription(request.getDescription());
            council.setDean(deanProfile);
            council.setStatus("ACTIVE");

            councilRepository.save(council);
            // Assign lecturers bằng email
            int assignedCount = 0;
            List<String> notFoundEmails = new ArrayList<>();
            List<String> notLecturerEmails = new ArrayList<>();

            if (request.getLecturerEmails() != null && !request.getLecturerEmails().isEmpty()) {
                for (String email : request.getLecturerEmails()) {
                    try {
                        // ✅ Tìm lecturer bằng email
                        Optional<Account> lecturerOpt = accountRepository.findByEmail(email.trim());

                        if (lecturerOpt.isEmpty()) {
                            notFoundEmails.add(email);
                            System.out.println("⚠️ Email not found: " + email);
                            continue;
                        }

                        Account lecturer = lecturerOpt.get();

                        // Kiểm tra phải là lecturer
                        if (lecturer.getRole() == null ||
                                !"LECTURER".equalsIgnoreCase(lecturer.getRole().getRoleName())) {
                            notLecturerEmails.add(email);
                            System.out.println("⚠️ Account " + email + " is not a lecturer, skipped");
                            continue;
                        }

                        // Kiểm tra đã assign chưa
                        if (councilMemberRepository.existsByCouncilIdAndLecturerId(
                                council.getId(), lecturer.getId())) {
                            System.out.println("⚠️ Lecturer " + email + " already in council, skipped");
                            continue;
                        }

                        CouncilMember member = new CouncilMember();
                        member.setCouncil(council);
                        member.setLecturer(lecturer);
                        member.setRole("Member");
                        member.setStatus("ACTIVE");

                        councilMemberRepository.save(member);
                        assignedCount++;

                        System.out.println("✅ Assigned lecturer: " + lecturer.getName() + " (" + email + ")");

                    } catch (Exception e) {
                        System.out.println("❌ Error assigning lecturer " + email + ": " + e.getMessage());
                    }
                }
            }

            System.out.println("✅ Total lecturers assigned: " + assignedCount);

            // ✅ Tạo message với thông tin chi tiết
            StringBuilder message = new StringBuilder("Council created successfully");
            if (assignedCount > 0) {
                message.append(" with ").append(assignedCount).append(" member(s)");
            }

            if (!notFoundEmails.isEmpty()) {
                message.append(". ⚠️ Email not found: ").append(String.join(", ", notFoundEmails));
            }

            if (!notLecturerEmails.isEmpty()) {
                message.append(". ⚠️ Not lecturer accounts: ").append(String.join(", ", notLecturerEmails));
            }

            CouncilResponse response = buildCouncilResponse(council);
            return ResponseDto.success(response, message.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * 2. Trưởng khoa/Admin gán project cho hội đồng
     */
    @Transactional
    public ResponseDto<String> assignProjectToCouncil(
            Integer projectId,
            Integer councilId,
            Authentication authentication) {
        try {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            Council council = councilRepository.findById(councilId)
                    .orElseThrow(() -> new RuntimeException("Council not found"));

            if (projectCouncilRepository.findByProjectIdAndCouncilId(projectId, councilId).isPresent()) {
                return ResponseDto.fail("Project already assigned to this council");
            }

            // ✅ Tạo assignment với enum
            ProjectCouncil projectCouncil = new ProjectCouncil();
            projectCouncil.setProject(project);
            projectCouncil.setCouncil(council);
            projectCouncil.setDecision(DecisionStatus.PENDING);  // ✅ Dùng enum

            projectCouncilRepository.save(projectCouncil);

            project.setStatus(ProjectStatus.IN_REVIEW);
            projectRepository.save(project);

            System.out.println("✅ Project " + projectId + " assigned to council " + council.getCouncilCode());

            return ResponseDto.success(null, "Project assigned to council successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * 3. Trưởng khoa xem danh sách projects cần duyệt
     */
    public ResponseDto<List<ProjectCouncilDto>> getMyPendingProjects(Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);
            // Kiểm tra role DEAN
            if (currentUser.getRole() == null ||
                    !"DEAN".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
                return ResponseDto.fail("Only deans can access this endpoint");
            }

            // Lấy dean profile
            CouncilManagerProfile deanProfile = councilProfileRepository
                    .findByAccountId(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Dean profile not found"));

            // Lấy tất cả projects PENDING của dean này
            List<ProjectCouncil> pendingProjects = projectCouncilRepository
                    .findPendingProjectsByDean(deanProfile.getId());

            System.out.println("✅ Found " + pendingProjects.size() + " pending projects for dean " + currentUser.getName());

            List<ProjectCouncilDto> dtos = pendingProjects.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return ResponseDto.success(dtos, "Get pending projects successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * 4. Trưởng khoa APPROVE/REJECT project
     */
    @Transactional
    public ResponseDto<String> makeDecision(
            Integer projectId,
            DeanDecisionRequest request,
            Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            if (currentUser.getRole() == null ||
                    !"DEAN".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
                return ResponseDto.fail("Only deans can make decisions");
            }

            CouncilManagerProfile deanProfile = councilProfileRepository
                    .findByAccountId(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Dean profile not found"));

            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            // ========== CASE 1: REJECT ==========
            if (request.getDecision() == DecisionStatus.REJECTED) {
                List<ProjectCouncil> projectCouncils = projectCouncilRepository.findByProjectId(projectId);

                if (!projectCouncils.isEmpty()) {
                    ProjectCouncil projectCouncil = projectCouncils.stream()
                            .filter(pc -> pc.getCouncil().getDean().getId().equals(deanProfile.getId()))
                            .findFirst()
                            .orElse(null);

                    if (projectCouncil != null) {
                        projectCouncil.setDecision(DecisionStatus.REJECTED);
                        projectCouncil.setComment(request.getComment());
                        projectCouncil.setDecisionDate(Instant.now());
                        projectCouncil.setDecidedBy(deanProfile);
                        projectCouncilRepository.save(projectCouncil);
                    }
                }

                project.setStatus(ProjectStatus.REJECTED);
                projectRepository.save(project);

                System.out.println("❌ Project " + projectId + " REJECTED by dean " + currentUser.getName());
                return ResponseDto.success(null, "Project rejected successfully");
            }

            // ========== CASE 2: APPROVE - KHÔNG CẦN COUNCIL ==========
            if (request.getDecision() == DecisionStatus.APPROVED) {

                // Kiểm tra project đã được approve chưa
                if (project.getStatus() == ProjectStatus.APPROVED) {
                    return ResponseDto.fail("Project already approved");
                }

                // ✅ Nếu đã có council (optional), cập nhật decision trong project_council
                List<ProjectCouncil> projectCouncils = projectCouncilRepository.findByProjectId(projectId);
                if (!projectCouncils.isEmpty()) {
                    ProjectCouncil projectCouncil = projectCouncils.stream()
                            .filter(pc -> pc.getCouncil().getDean().getId().equals(deanProfile.getId()))
                            .findFirst()
                            .orElse(null);

                    if (projectCouncil != null && projectCouncil.getDecision() == DecisionStatus.PENDING) {
                        projectCouncil.setDecision(DecisionStatus.APPROVED);
                        projectCouncil.setComment(request.getComment());
                        projectCouncil.setDecisionDate(Instant.now());
                        projectCouncil.setDecidedBy(deanProfile);
                        projectCouncilRepository.save(projectCouncil);
                    }
                }
                // ✅ Không có council cũng OK - Dean approve trực tiếp

                // Cập nhật project status
                project.setStatus(ProjectStatus.APPROVED);
                projectRepository.save(project);

                System.out.println("✅ Project " + projectId + " APPROVED by dean " + currentUser.getName());
                return ResponseDto.success(null, "Project approved successfully. Waiting for final report submission.");
            }

            // ========== CASE 3: Invalid decision ==========
            return ResponseDto.fail("Decision must be APPROVED or REJECTED");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * 5. Xem tất cả councils của trưởng khoa
     */
    public ResponseDto<List<CouncilResponse>> getMyCouncils(Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            CouncilManagerProfile deanProfile = councilProfileRepository
                    .findByAccountId(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Dean profile not found"));

            List<Council> councils = councilRepository.findByDeanId(deanProfile.getId());

            System.out.println("✅ Found " + councils.size() + " councils for dean " + currentUser.getName());

            List<CouncilResponse> responses = councils.stream()
                    .map(this::buildCouncilResponse)
                    .collect(Collectors.toList());

            return ResponseDto.success(responses, "Get councils successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * 6. Xem chi tiết một council
     */
    public ResponseDto<CouncilResponse> getCouncilDetail(Integer councilId) {
        try {
            Council council = councilRepository.findById(councilId)
                    .orElseThrow(() -> new RuntimeException("Council not found"));

            CouncilResponse response = buildCouncilResponse(council);

            return ResponseDto.success(response, "Get council detail successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * 7. Xem tất cả projects của một council
     */
    public ResponseDto<List<ProjectCouncilDto>> getProjectsByCouncil(Integer councilId) {
        try {
            Council council = councilRepository.findById(councilId)
                    .orElseThrow(() -> new RuntimeException("Council not found"));

            List<ProjectCouncil> projectCouncils = projectCouncilRepository.findByCouncilId(councilId);

            List<ProjectCouncilDto> dtos = projectCouncils.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return ResponseDto.success(dtos, "Get projects successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================

    private CouncilResponse buildCouncilResponse(Council council) {
        List<CouncilMember> members = councilMemberRepository.findByCouncilId(council.getId());

        List<CouncilResponse.MemberInfo> memberInfos = members.stream()
                .map(m -> CouncilResponse.MemberInfo.builder()
                        .id(m.getId())
                        .lecturerId(m.getLecturer().getId())
                        .lecturerName(m.getLecturer().getName())
                        .lecturerEmail(m.getLecturer().getEmail())
                        .role(m.getRole())
                        .status(m.getStatus())
                        .build())
                .collect(Collectors.toList());

        return CouncilResponse.builder()
                .id(council.getId())
                .councilCode(council.getCouncilCode())
                .councilName(council.getCouncilName())
                .department(council.getDepartment())
                .description(council.getDescription())
                .status(council.getStatus())
                .createdAt(council.getCreatedAt())
                .deanId(council.getDean().getId())
                .deanName(council.getDean().getAccount().getName())
                .deanEmail(council.getDean().getAccount().getEmail())
                .members(memberInfos)
                .build();
    }

    private ProjectCouncilDto convertToDto(ProjectCouncil pc) {
        return ProjectCouncilDto.builder()
                .id(pc.getId())
                .projectId(pc.getProject().getId())
                .projectName(pc.getProject().getName())
                .projectDescription(pc.getProject().getDescription())
                .projectStatus(pc.getProject().getStatus())
                .councilCode(pc.getCouncil().getCouncilCode())
                .councilName(pc.getCouncil().getCouncilName())
                .decision(pc.getDecision())
                .comment(pc.getComment())
                .decisionDate(pc.getDecisionDate())
                .build();
    }

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

    /**
     * API cho giảng viên xem các hội đồng mà mình là thành viên
     */
    public ResponseDto<List<CouncilResponse>> getMyJoinedCouncils(Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            if (!"LECTURER".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
                return ResponseDto.fail("Only lecturers can access this endpoint");
            }

            // Tìm các council mà lecturer này là member
            List<CouncilMember> myMemberships = councilMemberRepository
                    .findByLecturerId(currentUser.getId());

            if (myMemberships.isEmpty()) {
                return ResponseDto.success(new ArrayList<>(),
                        "You are not a member of any council");
            }

            System.out.println("✅ Found " + myMemberships.size() +
                    " council membership(s) for lecturer " + currentUser.getName());

            List<CouncilResponse> responses = myMemberships.stream()
                    .map(cm -> {
                        Council council = cm.getCouncil();
                        CouncilResponse response = buildCouncilResponse(council);

                        // Thêm thông tin về role của mình trong hội đồng này
                        // (Có thể extend CouncilResponse để thêm field myRole nếu cần)
                        return response;
                    })
                    .collect(Collectors.toList());

            return ResponseDto.success(responses,
                    "Found " + responses.size() + " council(s)");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * UPDATE council - Cập nhật thông tin và thêm members
     */
    @Transactional
    public ResponseDto<CouncilResponse> updateCouncil(
            Integer councilId,
            CreateCouncilRequest request,
            Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            // Check role DEAN
            if (currentUser.getRole() == null ||
                    !"DEAN".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
                return ResponseDto.fail("Only deans can update councils");
            }

            // Find council
            Council council = councilRepository.findById(councilId)
                    .orElseThrow(() -> new RuntimeException("Council not found"));

            // Check authorization
            CouncilManagerProfile deanProfile = councilProfileRepository
                    .findByAccountId(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Dean profile not found"));

            if (!council.getDean().getId().equals(deanProfile.getId())) {
                return ResponseDto.fail("You are not authorized to update this council");
            }

            // Update basic info
            if (request.getCouncilName() != null && !request.getCouncilName().isBlank()) {
                council.setCouncilName(request.getCouncilName());
            }
            if (request.getDepartment() != null && !request.getDepartment().isBlank()) {
                council.setDepartment(request.getDepartment());
            }
            if (request.getDescription() != null) {
                council.setDescription(request.getDescription());
            }
            councilRepository.save(council);

            // Add new members
            int addedCount = 0;
            int alreadyExistsCount = 0;
            List<String> notFoundEmails = new ArrayList<>();
            List<String> notLecturerEmails = new ArrayList<>();

            if (request.getLecturerEmails() != null && !request.getLecturerEmails().isEmpty()) {
                for (String email : request.getLecturerEmails()) {
                    try {
                        Optional<Account> lecturerOpt = accountRepository.findByEmail(email.trim());

                        if (lecturerOpt.isEmpty()) {
                            notFoundEmails.add(email);
                            continue;
                        }

                        Account lecturer = lecturerOpt.get();

                        if (lecturer.getRole() == null ||
                                !"LECTURER".equalsIgnoreCase(lecturer.getRole().getRoleName())) {
                            notLecturerEmails.add(email);
                            continue;
                        }

                        if (councilMemberRepository.existsByCouncilIdAndLecturerId(
                                council.getId(), lecturer.getId())) {
                            alreadyExistsCount++;
                            continue;
                        }

                        CouncilMember member = new CouncilMember();
                        member.setCouncil(council);
                        member.setLecturer(lecturer);
                        member.setRole("Member");
                        member.setStatus("ACTIVE");
                        councilMemberRepository.save(member);
                        addedCount++;

                    } catch (Exception e) {
                        System.err.println("Error adding lecturer " + email + ": " + e.getMessage());
                    }
                }
            }

            // Build response message
            StringBuilder message = new StringBuilder("Council updated successfully");
            if (addedCount > 0) {
                message.append(". Added ").append(addedCount).append(" new member(s)");
            }
            if (alreadyExistsCount > 0) {
                message.append(". ").append(alreadyExistsCount).append(" member(s) already exists");
            }
            if (!notFoundEmails.isEmpty()) {
                message.append(". ⚠️ Email not found: ").append(String.join(", ", notFoundEmails));
            }
            if (!notLecturerEmails.isEmpty()) {
                message.append(". ⚠️ Not lecturer: ").append(String.join(", ", notLecturerEmails));
            }

            CouncilResponse response = buildCouncilResponse(council);
            return ResponseDto.success(response, message.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * DELETE council - Soft delete (status = INACTIVE)
     */
    @Transactional
    public ResponseDto<String> deleteCouncil(
            Integer councilId,
            Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            // Check role DEAN
            if (!"DEAN".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
                return ResponseDto.fail("Only deans can delete councils");
            }

            // Find council
            Council council = councilRepository.findById(councilId)
                    .orElseThrow(() -> new RuntimeException("Council not found"));

            // Check authorization
            CouncilManagerProfile deanProfile = councilProfileRepository
                    .findByAccountId(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Dean profile not found"));

            if (!council.getDean().getId().equals(deanProfile.getId())) {
                return ResponseDto.fail("You are not authorized to delete this council");
            }

            // Check pending projects
            List<ProjectCouncil> activeProjects = projectCouncilRepository
                    .findByCouncilId(councilId).stream()
                    .filter(pc -> pc.getDecision() == DecisionStatus.PENDING)
                    .collect(Collectors.toList());

            if (!activeProjects.isEmpty()) {
                return ResponseDto.fail(
                        "Cannot delete council. There are " + activeProjects.size() +
                                " pending project(s) being reviewed"
                );
            }

            // Soft delete
            council.setStatus("INACTIVE");
            councilRepository.save(council);

            return ResponseDto.success(null,
                    "Council deleted successfully (status changed to INACTIVE)");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * REMOVE member từ council
     */
    @Transactional
    public ResponseDto<String> removeMemberFromCouncil(
            Integer councilId,
            Integer lecturerId,
            Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            // Check role DEAN
            if (!"DEAN".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
                return ResponseDto.fail("Only deans can remove members");
            }

            // Find council
            Council council = councilRepository.findById(councilId)
                    .orElseThrow(() -> new RuntimeException("Council not found"));

            // Check authorization
            CouncilManagerProfile deanProfile = councilProfileRepository
                    .findByAccountId(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Dean profile not found"));

            if (!council.getDean().getId().equals(deanProfile.getId())) {
                return ResponseDto.fail("You are not authorized to modify this council");
            }

            // Find and delete member
            CouncilMember member = councilMemberRepository
                    .findByCouncilIdAndLecturerId(councilId, lecturerId)
                    .orElseThrow(() -> new RuntimeException("Member not found in this council"));

            councilMemberRepository.delete(member);

            return ResponseDto.success(null,
                    "Member removed from council successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * GET ALL COUNCILS - For ADMIN only
     */
    public ResponseDto<PageResponse<CouncilResponse>> getAllCouncils(
            int page,
            int size,
            String name,
            String status,
            Integer deanId,
            Authentication authentication) {
        try {
            // ✅ SỬA: Cho phép cả ADMIN và DEAN
            Account currentUser = getCurrentAccount(authentication);
            String roleName = currentUser.getRole() != null
                    ? currentUser.getRole().getRoleName()
                    : "";

            if (!"ADMIN".equalsIgnoreCase(roleName) && !"DEAN".equalsIgnoreCase(roleName)) {
                return ResponseDto.fail("Only admins and deans can access all councils");
            }

            Pageable pageable = PageRequest.of(page - 1, size);

            // Build specification
            Specification<Council> spec = buildCouncilSpecification(name, status, deanId);

            Page<Council> councilPage = councilRepository.findAll(spec, pageable);

            // Map to response
            List<CouncilResponse> responseList = councilPage.getContent().stream()
                    .map(council -> {
                        CouncilResponse response = buildCouncilResponse(council);

                        List<ProjectCouncil> projectCouncils = projectCouncilRepository.findByCouncilId(council.getId());
                        response.setTotalProjects(projectCouncils.size());

                        return response;
                    })
                    .collect(Collectors.toList());

            PageResponse<CouncilResponse> pageResponse = PageResponse.<CouncilResponse>builder()
                    .currentPages(page)
                    .pageSizes(pageable.getPageSize())
                    .totalPages(councilPage.getTotalPages())
                    .totalElements((int) councilPage.getTotalElements())
                    .data(responseList)
                    .build();

            return ResponseDto.success(pageResponse,
                    "Found " + councilPage.getTotalElements() + " council(s)");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * Build specification for council filtering
     */
    private Specification<Council> buildCouncilSpecification(
            String name,
            String status,
            Integer deanId) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by name (search both councilName and councilCode)
            if (name != null && !name.isBlank()) {
                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("councilName")),
                                        "%" + name.toLowerCase().trim() + "%"
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("councilCode")),
                                        "%" + name.toLowerCase().trim() + "%"
                                )
                        )
                );
            }

            // Filter by status
            if (status != null && !status.isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.upper(root.get("status")),
                                status.toUpperCase()
                        )
                );
            }

            // Filter by dean ID
            if (deanId != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("dean").get("id"), deanId)
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 8. Lấy danh sách projects có Final Report nhưng chưa được assign vào hội đồng nào
     * Để Dean biết project nào cần tạo hội đồng chấm điểm
     */
    public ResponseDto<List<ProjectReadyForCouncilDto>> getProjectsReadyForCouncil(Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            // Chỉ DEAN mới được xem
            if (!"DEAN".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
                return ResponseDto.fail("Only deans can access this endpoint");
            }

            // Lấy tất cả projects đã APPROVED và có Final Report
            List<Project> approvedProjects = projectRepository.findByStatus(ProjectStatus.APPROVED);

            List<ProjectReadyForCouncilDto> result = new ArrayList<>();

            for (Project project : approvedProjects) {
                // Kiểm tra có Final Report chưa
                Optional<Milestone> finalMilestoneOpt = milestoneRepository
                        .findFirstByProjectIdAndIsFinalOrderByIdDesc(project.getId(), true);

                if (finalMilestoneOpt.isEmpty()) {
                    continue; // Chưa có final report → skip
                }

                Milestone finalMilestone = finalMilestoneOpt.get();

                // Kiểm tra đã được assign vào council nào chưa
                List<ProjectCouncil> existingAssignments = projectCouncilRepository
                        .findByProjectId(project.getId());

                boolean alreadyAssigned = !existingAssignments.isEmpty();

                // Lấy thông tin owner
                Integer ownerId = null;
                String ownerName = null;
                String ownerEmail = null;
                String ownerRole = null;

                if (project.getOwner() != null) {
                    ownerId = project.getOwner().getId();
                    ownerName = project.getOwner().getName();
                    ownerEmail = project.getOwner().getEmail();
                    ownerRole = project.getOwner().getRole() != null
                            ? project.getOwner().getRole().getRoleName()
                            : null;
                }

                // Lấy thông tin council nếu đã assign
                Integer assignedCouncilId = null;
                String assignedCouncilName = null;
                String assignedCouncilCode = null;

                if (alreadyAssigned) {
                    ProjectCouncil pc = existingAssignments.get(0);
                    assignedCouncilId = pc.getCouncil().getId();
                    assignedCouncilName = pc.getCouncil().getCouncilName();
                    assignedCouncilCode = pc.getCouncil().getCouncilCode();
                }

                ProjectReadyForCouncilDto dto = ProjectReadyForCouncilDto.builder()
                        .projectId(project.getId())
                        .projectName(project.getName())
                        .projectDescription(project.getDescription())
                        .projectType(project.getType())
                        .projectStatus(project.getStatus().toString())
                        .projectCreateDate(project.getCreateDate())
                        .projectDueDate(project.getDueDate())

                        // Owner info
                        .ownerId(ownerId)
                        .ownerName(ownerName)
                        .ownerEmail(ownerEmail)
                        .ownerRole(ownerRole)

                        // Major info
                        .majorId(project.getMajor() != null ? project.getMajor().getId() : null)
                        .majorName(project.getMajor() != null ? project.getMajor().getName() : null)

                        // Final Report info
                        .hasFinalReport(true)
                        .finalMilestoneId(finalMilestone.getId())
                        .reportUrl(finalMilestone.getReportUrl())
                        .reportSubmittedAt(finalMilestone.getReportSubmittedAt())
                        .reportSubmittedBy(finalMilestone.getReportSubmittedBy() != null
                                ? finalMilestone.getReportSubmittedBy().getName()
                                : null)

                        // Council assignment info
                        .alreadyAssignedToCouncil(alreadyAssigned)
                        .assignedCouncilId(assignedCouncilId)
                        .assignedCouncilName(assignedCouncilName)
                        .assignedCouncilCode(assignedCouncilCode)

                        .build();

                result.add(dto);
            }

            // Sort: Chưa assign lên trước
            result.sort((a, b) -> {
                if (a.getAlreadyAssignedToCouncil() != b.getAlreadyAssignedToCouncil()) {
                    return a.getAlreadyAssignedToCouncil() ? 1 : -1;
                }
                return 0;
            });

            long notAssignedCount = result.stream()
                    .filter(p -> !p.getAlreadyAssignedToCouncil())
                    .count();

            String message = String.format(
                    "Found %d project(s) with final report. %d not yet assigned to council.",
                    result.size(), notAssignedCount
            );

            return ResponseDto.success(result, message);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * 9. Tạo hội đồng VÀ assign project vào hội đồng luôn
     * Flow: Dean chọn project có final report → Tạo hội đồng → Assign project vào
     */
    @Transactional
    public ResponseDto<CouncilResponse> createCouncilAndAssignProject(
            CreateCouncilWithProjectRequest request,
            Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            // Kiểm tra role DEAN
            if (!"DEAN".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
                return ResponseDto.fail("Only deans can create councils");
            }

            // Lấy dean profile
            CouncilManagerProfile deanProfile = councilProfileRepository
                    .findByAccountId(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Dean profile not found"));

            // Kiểm tra project tồn tại
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            // Kiểm tra project có final report chưa
            Optional<Milestone> finalMilestoneOpt = milestoneRepository
                    .findFirstByProjectIdAndIsFinalOrderByIdDesc(project.getId(), true);

            if (finalMilestoneOpt.isEmpty()) {
                return ResponseDto.fail("Project must have a final report before creating council");
            }

            // Kiểm tra project đã được assign vào council khác chưa
            List<ProjectCouncil> existingAssignments = projectCouncilRepository
                    .findByProjectId(project.getId());

            if (!existingAssignments.isEmpty()) {
                return ResponseDto.fail("Project already assigned to council: " +
                        existingAssignments.get(0).getCouncil().getCouncilCode());
            }

            // Kiểm tra council code đã tồn tại chưa
            if (councilRepository.findByCouncilCode(request.getCouncilCode()).isPresent()) {
                return ResponseDto.fail("Council code already exists");
            }

            // ========== TẠO COUNCIL ==========
            Council council = new Council();
            council.setCouncilCode(request.getCouncilCode());
            council.setCouncilName(request.getCouncilName());
            council.setDepartment(request.getDepartment());
            council.setDescription(request.getDescription());
            council.setDean(deanProfile);
            council.setStatus("ACTIVE");

            councilRepository.save(council);

            // ========== ASSIGN LECTURERS ==========
            int assignedCount = 0;
            List<String> notFoundEmails = new ArrayList<>();
            List<String> notLecturerEmails = new ArrayList<>();

            if (request.getLecturerEmails() != null && !request.getLecturerEmails().isEmpty()) {
                for (String email : request.getLecturerEmails()) {
                    try {
                        Optional<Account> lecturerOpt = accountRepository.findByEmail(email.trim());

                        if (lecturerOpt.isEmpty()) {
                            notFoundEmails.add(email);
                            continue;
                        }

                        Account lecturer = lecturerOpt.get();

                        if (!"LECTURER".equalsIgnoreCase(lecturer.getRole().getRoleName())) {
                            notLecturerEmails.add(email);
                            continue;
                        }

                        if (councilMemberRepository.existsByCouncilIdAndLecturerId(
                                council.getId(), lecturer.getId())) {
                            continue;
                        }

                        CouncilMember member = new CouncilMember();
                        member.setCouncil(council);
                        member.setLecturer(lecturer);
                        member.setRole("Member");
                        member.setStatus("ACTIVE");
                        councilMemberRepository.save(member);
                        assignedCount++;

                    } catch (Exception e) {
                        System.err.println("Error adding lecturer " + email + ": " + e.getMessage());
                    }
                }
            }

            // ========== ASSIGN PROJECT VÀO COUNCIL ==========
            ProjectCouncil projectCouncil = new ProjectCouncil();
            projectCouncil.setProject(project);
            projectCouncil.setCouncil(council);
            projectCouncil.setDecision(DecisionStatus.PENDING);
            projectCouncilRepository.save(projectCouncil);

            // Update project status
            project.setStatus(ProjectStatus.IN_REVIEW);
            projectRepository.save(project);

            System.out.println("✅ Council " + council.getCouncilCode() + " created with " +
                    assignedCount + " member(s) and project " + project.getName() + " assigned");

            // Build response message
            StringBuilder message = new StringBuilder();
            message.append("Council created and project assigned successfully");
            if (assignedCount > 0) {
                message.append(". ").append(assignedCount).append(" lecturer(s) added");
            }
            if (!notFoundEmails.isEmpty()) {
                message.append(". ⚠️ Email not found: ").append(String.join(", ", notFoundEmails));
            }
            if (!notLecturerEmails.isEmpty()) {
                message.append(". ⚠️ Not lecturer: ").append(String.join(", ", notLecturerEmails));
            }

            CouncilResponse response = buildCouncilResponse(council);
            response.setAssignedProjectId(project.getId());
            response.setAssignedProjectName(project.getName());

            return ResponseDto.success(response, message.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }


}
