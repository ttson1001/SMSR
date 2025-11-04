package com.example.smrsservice.service;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.project.ProjectMemberResponse;
import com.example.smrsservice.entity.Account;
import com.example.smrsservice.entity.ProjectMember;
import com.example.smrsservice.repository.AccountRepository;
import com.example.smrsservice.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;
    private final AccountRepository accountRepository;

    private static final int MAX_STUDENTS_PER_PROJECT = 5;

    /**
     * Lấy tất cả lời mời của user hiện tại (status = Pending)
     */
    public ResponseDto<List<ProjectMemberResponse>> getMyInvitations() {
        try {
            Account currentUser = getCurrentAccount();

            List<ProjectMember> invitations = projectMemberRepository
                    .findByAccountIdAndStatus(currentUser.getId(), "Pending");

            List<ProjectMemberResponse> responses = invitations.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseDto.success(responses, "Get invitations successfully");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * Approve lời mời với validation
     */
    @Transactional
    public ResponseDto<String> approveInvitation(Integer invitationId) {
        try {
            Account currentUser = getCurrentAccount();

            ProjectMember invitation = projectMemberRepository.findById(invitationId)
                    .orElseThrow(() -> new RuntimeException("Invitation not found"));

            // Kiểm tra lời mời có phải của user hiện tại không
            if (!invitation.getAccount().getId().equals(currentUser.getId())) {
                return ResponseDto.fail("This invitation does not belong to you");
            }

            // Kiểm tra trạng thái
            if (!"Pending".equals(invitation.getStatus())) {
                return ResponseDto.fail("This invitation has already been processed");
            }

            // Kiểm tra user có đang tham gia project active nào không
            boolean hasActiveProject = projectMemberRepository.hasActiveProject(currentUser.getId());
            if (hasActiveProject) {
                return ResponseDto.fail("You are already in an active project. Please complete it before joining another project.");
            }

            // Validate theo role trước khi approve
            if ("LECTURER".equalsIgnoreCase(invitation.getMemberRole())) {
                // Kiểm tra đã có giảng viên chưa
                Optional<ProjectMember> existingLecturer = projectMemberRepository
                        .findLecturerByProjectId(invitation.getProject().getId());

                if (existingLecturer.isPresent()) {
                    return ResponseDto.fail("This project already has a lecturer");
                }

            } else if ("STUDENT".equalsIgnoreCase(invitation.getMemberRole())) {
                // Kiểm tra số lượng sinh viên
                long currentStudents = projectMemberRepository
                        .countByProjectIdAndMemberRoleAndStatus(
                                invitation.getProject().getId(),
                                "STUDENT",
                                "Approved"
                        );

                if (currentStudents >= MAX_STUDENTS_PER_PROJECT) {
                    return ResponseDto.fail("Maximum " + MAX_STUDENTS_PER_PROJECT + " students reached");
                }
            }

            // Approve lời mời
            invitation.setStatus("Approved");
            projectMemberRepository.save(invitation);

            return ResponseDto.success("Invitation approved successfully");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * Cancel/Reject lời mời
     */
    @Transactional
    public ResponseDto<String> cancelInvitation(Integer invitationId) {
        try {
            Account currentUser = getCurrentAccount();

            ProjectMember invitation = projectMemberRepository.findById(invitationId)
                    .orElseThrow(() -> new RuntimeException("Invitation not found"));

            // Kiểm tra lời mời có phải của user hiện tại không
            if (!invitation.getAccount().getId().equals(currentUser.getId())) {
                return ResponseDto.fail("This invitation does not belong to you");
            }

            // Kiểm tra trạng thái
            if ("Cancelled".equals(invitation.getStatus())) {
                return ResponseDto.fail("This invitation has already been cancelled");
            }

            invitation.setStatus("Cancelled");
            projectMemberRepository.save(invitation);

            return ResponseDto.success("Invitation cancelled successfully");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * Lấy tất cả project mà user đang tham gia (status = Approved)
     */
    public ResponseDto<List<ProjectMemberResponse>> getMyProjects() {
        try {
            Account currentUser = getCurrentAccount();

            List<ProjectMember> projects = projectMemberRepository
                    .findByAccountIdAndStatus(currentUser.getId(), "Approved");

            List<ProjectMemberResponse> responses = projects.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseDto.success(responses, "Get my projects successfully");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * Lấy project đang active của user (nếu có)
     */
    public ResponseDto<ProjectMemberResponse> getMyActiveProject() {
        try {
            Account currentUser = getCurrentAccount();

            List<ProjectMember> activeProjects = projectMemberRepository
                    .findActiveProjectsByAccountId(currentUser.getId());

            if (activeProjects.isEmpty()) {
                return ResponseDto.success(null, "No active project found");
            }

            // Lấy project đầu tiên (vì chỉ có 1 project active)
            ProjectMemberResponse response = convertToResponse(activeProjects.get(0));

            return ResponseDto.success(response, "Get active project successfully");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * Lấy danh sách thành viên của project
     */
    public ResponseDto<List<ProjectMemberResponse>> getProjectMembers(Integer projectId) {
        try {
            List<ProjectMember> members = projectMemberRepository
                    .findByProjectIdAndStatus(projectId, "Approved");

            List<ProjectMemberResponse> responses = members.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseDto.success(responses, "Get project members successfully");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    private ProjectMemberResponse convertToResponse(ProjectMember member) {
        return ProjectMemberResponse.builder()
                .id(member.getId())
                .projectId(member.getProject().getId())
                .projectName(member.getProject().getName())
                .projectDescription(member.getProject().getDescription())
                .projectType(member.getProject().getType())
                .memberRole(member.getMemberRole())
                .ownerName(member.getProject().getOwner().getName())
                .ownerEmail(member.getProject().getOwner().getEmail())
                .status(member.getStatus())
                .createDate(member.getProject().getCreateDate())
                .dueDate(member.getProject().getDueDate())
                .build();
    }

    private Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
