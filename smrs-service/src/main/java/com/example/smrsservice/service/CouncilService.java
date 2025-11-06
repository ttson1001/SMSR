package com.example.smrsservice.service;

import com.example.smrsservice.common.ProjectStatus;
import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.concil.CouncilResponse;
import com.example.smrsservice.dto.concil.CreateCouncilRequest;
import com.example.smrsservice.dto.concil.DeanDecisionRequest;
import com.example.smrsservice.dto.concil.ProjectCouncilDto;
import com.example.smrsservice.entity.*;
import com.example.smrsservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
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

            System.out.println("✅ Council created: " + council.getCouncilCode());

            // Assign lecturers vào hội đồng
            int assignedCount = 0;
            if (request.getLecturerIds() != null && !request.getLecturerIds().isEmpty()) {
                for (Integer lecturerId : request.getLecturerIds()) {
                    try {
                        Account lecturer = accountRepository.findById(lecturerId)
                                .orElseThrow(() -> new RuntimeException("Lecturer not found: " + lecturerId));

                        // Kiểm tra phải là lecturer
                        if (lecturer.getRole() == null ||
                                !"LECTURER".equalsIgnoreCase(lecturer.getRole().getRoleName())) {
                            System.out.println("⚠️ Account " + lecturerId + " is not a lecturer, skipped");
                            continue;
                        }

                        // Kiểm tra đã assign chưa
                        if (councilMemberRepository.existsByCouncilIdAndLecturerId(council.getId(), lecturerId)) {
                            System.out.println("⚠️ Lecturer " + lecturerId + " already in council, skipped");
                            continue;
                        }

                        CouncilMember member = new CouncilMember();
                        member.setCouncil(council);
                        member.setLecturer(lecturer);
                        member.setRole("Member");
                        member.setStatus("ACTIVE");

                        councilMemberRepository.save(member);
                        assignedCount++;

                        System.out.println("✅ Assigned lecturer: " + lecturer.getName());

                    } catch (Exception e) {
                        System.out.println("❌ Error assigning lecturer " + lecturerId + ": " + e.getMessage());
                    }
                }
            }

            System.out.println("✅ Total lecturers assigned: " + assignedCount);

            CouncilResponse response = buildCouncilResponse(council);
            return ResponseDto.success(response, "Council created successfully with " + assignedCount + " members");

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

            // Kiểm tra đã gán chưa
            if (projectCouncilRepository.findByProjectIdAndCouncilId(projectId, councilId).isPresent()) {
                return ResponseDto.fail("Project already assigned to this council");
            }

            // Tạo assignment
            ProjectCouncil projectCouncil = new ProjectCouncil();
            projectCouncil.setProject(project);
            projectCouncil.setCouncil(council);
            projectCouncil.setDecision("PENDING");

            projectCouncilRepository.save(projectCouncil);

            // Cập nhật project status
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

            // Kiểm tra role DEAN
            if (currentUser.getRole() == null ||
                    !"DEAN".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
                return ResponseDto.fail("Only deans can make decisions");
            }

            // Lấy dean profile
            CouncilManagerProfile deanProfile = councilProfileRepository
                    .findByAccountId(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Dean profile not found"));

            // Lấy project
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            // Tìm project council assignment
            List<ProjectCouncil> projectCouncils = projectCouncilRepository.findByProjectId(projectId);
            if (projectCouncils.isEmpty()) {
                return ResponseDto.fail("Project is not assigned to any council");
            }

            // Kiểm tra dean có quyền duyệt không (phải là dean của council được assigned)
            ProjectCouncil projectCouncil = projectCouncils.stream()
                    .filter(pc -> pc.getCouncil().getDean().getId().equals(deanProfile.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("You are not authorized to review this project"));

            // Kiểm tra đã quyết định chưa
            if (!"PENDING".equals(projectCouncil.getDecision())) {
                return ResponseDto.fail("Decision already made for this project: " + projectCouncil.getDecision());
            }

            // Validate decision
            if (!"APPROVED".equals(request.getDecision()) && !"REJECTED".equals(request.getDecision())) {
                return ResponseDto.fail("Decision must be APPROVED or REJECTED");
            }

            // Cập nhật decision
            projectCouncil.setDecision(request.getDecision());
            projectCouncil.setComment(request.getComment());
            projectCouncil.setDecisionDate(Instant.now());
            projectCouncil.setDecidedBy(deanProfile);

            projectCouncilRepository.save(projectCouncil);

            // Cập nhật project status
            if ("APPROVED".equals(request.getDecision())) {
                project.setStatus(ProjectStatus.APPROVED);
                System.out.println("✅ Project " + projectId + " APPROVED by dean " + currentUser.getName());
            } else {
                project.setStatus(ProjectStatus.REJECTED);
                System.out.println("❌ Project " + projectId + " REJECTED by dean " + currentUser.getName());
            }
            projectRepository.save(project);

            return ResponseDto.success(null, "Decision submitted successfully: " + request.getDecision());

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
}
