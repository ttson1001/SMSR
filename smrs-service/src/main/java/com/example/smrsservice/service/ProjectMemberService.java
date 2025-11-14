package com.example.smrsservice.service;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.project.InviteMemberResponse;
import com.example.smrsservice.dto.project.ProjectMemberResponse;
import com.example.smrsservice.entity.Account;
import com.example.smrsservice.entity.Project;
import com.example.smrsservice.entity.ProjectMember;
import com.example.smrsservice.repository.AccountRepository;
import com.example.smrsservice.repository.ProjectMemberRepository;
import com.example.smrsservice.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;
    private final AccountRepository accountRepository;
    private final ProjectRepository projectRepository;
    private final MailService mailService;

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
    public ResponseDto<ProjectMemberResponse> approveInvitation(Integer invitationId) {
        try {
            Account currentUser = getCurrentAccount();

            ProjectMember invitation = projectMemberRepository.findById(invitationId)
                    .orElseThrow(() -> new RuntimeException("Invitation not found"));

            if (!invitation.getAccount().getId().equals(currentUser.getId())) {
                return ResponseDto.fail("This invitation does not belong to you");
            }

            if (!"Pending".equals(invitation.getStatus())) {
                return ResponseDto.fail("This invitation has already been processed");
            }

            if ("LECTURER".equalsIgnoreCase(invitation.getMemberRole())) {
                Optional<ProjectMember> existingLecturer = projectMemberRepository
                        .findLecturerByProjectId(invitation.getProject().getId());

                if (existingLecturer.isPresent()) {
                    return ResponseDto.fail("This project already has a lecturer mentor");
                }

            } else if ("STUDENT".equalsIgnoreCase(invitation.getMemberRole())) {
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

            invitation.setStatus("Approved");
            projectMemberRepository.save(invitation);

            ProjectMemberResponse response = convertToResponse(invitation);

            return ResponseDto.success(response, "Invitation approved successfully");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * Cancel/Reject lời mời
     */
    @Transactional
    public ResponseDto<ProjectMemberResponse> cancelInvitation(Integer invitationId) {
        try {
            Account currentUser = getCurrentAccount();

            ProjectMember invitation = projectMemberRepository.findById(invitationId)
                    .orElseThrow(() -> new RuntimeException("Invitation not found"));

            if (!invitation.getAccount().getId().equals(currentUser.getId())) {
                return ResponseDto.fail("This invitation does not belong to you");
            }

            if ("Cancelled".equals(invitation.getStatus())) {
                return ResponseDto.fail("This invitation has already been cancelled");
            }

            invitation.setStatus("Cancelled");
            projectMemberRepository.save(invitation);

            ProjectMemberResponse response = convertToResponse(invitation);

            return ResponseDto.success(response, "Invitation cancelled successfully");

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
     * Lấy tất cả active projects của user
     */
    public ResponseDto<List<ProjectMemberResponse>> getMyActiveProjects() {
        try {
            Account currentUser = getCurrentAccount();

            List<ProjectMember> activeProjects = projectMemberRepository
                    .findActiveProjectsByAccountId(currentUser.getId());

            List<ProjectMemberResponse> responses = activeProjects.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseDto.success(responses, "Get active projects successfully");
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

    /**
     * Mời thêm members vào project
     */
    @Transactional
    public ResponseDto<InviteMemberResponse> inviteMembers(
            Integer projectId,
            List<String> emails,
            Authentication authentication) {

        try {
            Account currentUser = getCurrentAccount(authentication);

            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));

            if (!isOwner && !isAdmin) {
                return ResponseDto.fail("Only project owner or admin can invite members");
            }

            List<String> successEmails = new ArrayList<>();
            List<String> failedEmails = new ArrayList<>();
            List<String> failedReasons = new ArrayList<>();

            long currentLecturers = projectMemberRepository.countByProjectIdAndMemberRoleAndStatus(
                    projectId, "LECTURER", "Approved");
            long currentStudents = projectMemberRepository.countByProjectIdAndMemberRoleAndStatus(
                    projectId, "STUDENT", "Approved");

            int lecturerCount = 0;
            int studentCount = 0;

            for (String email : emails) {
                String trimmedEmail = email.trim();

                try {
                    Account invitedAccount = accountRepository.findByEmail(trimmedEmail)
                            .orElse(null);

                    if (invitedAccount == null) {
                        failedEmails.add(trimmedEmail);
                        failedReasons.add(trimmedEmail + ": Account not found");
                        continue;
                    }

                    if (invitedAccount.getId().equals(project.getOwner().getId())) {
                        failedEmails.add(trimmedEmail);
                        failedReasons.add(trimmedEmail + ": Cannot invite project owner");
                        continue;
                    }

                    if (invitedAccount.getRole() == null) {
                        failedEmails.add(trimmedEmail);
                        failedReasons.add(trimmedEmail + ": Account has no role");
                        continue;
                    }

                    boolean alreadyInvited = projectMemberRepository
                            .existsByProjectIdAndAccountId(projectId, invitedAccount.getId());
                    if (alreadyInvited) {
                        failedEmails.add(trimmedEmail);
                        failedReasons.add(trimmedEmail + ": Already invited");
                        continue;
                    }

                    String roleName = invitedAccount.getRole().getRoleName();

                    if ("LECTURER".equalsIgnoreCase(roleName)) {
                        if (currentLecturers > 0 || lecturerCount > 0) {
                            failedEmails.add(trimmedEmail);
                            failedReasons.add(trimmedEmail + ": Project already has a lecturer mentor");
                            continue;
                        }
                        lecturerCount++;

                    } else if ("STUDENT".equalsIgnoreCase(roleName)) {
                        if (currentStudents + studentCount >= MAX_STUDENTS_PER_PROJECT) {
                            failedEmails.add(trimmedEmail);
                            failedReasons.add(trimmedEmail + ": Maximum students reached (5)");
                            continue;
                        }
                        studentCount++;

                    } else {
                        failedEmails.add(trimmedEmail);
                        failedReasons.add(trimmedEmail + ": Invalid role");
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
                                currentUser.getName(),
                                roleName.toUpperCase(),
                                member.getId(),
                                invitationToken
                        );
                    } catch (Exception emailEx) {
                        System.err.println("Failed to send email to " + invitedAccount.getEmail() + ": " + emailEx.getMessage());
                    }

                    successEmails.add(trimmedEmail);

                } catch (Exception e) {
                    failedEmails.add(trimmedEmail);
                    failedReasons.add(trimmedEmail + ": " + e.getMessage());
                }
            }

            InviteMemberResponse response = InviteMemberResponse.builder()
                    .totalInvited(emails.size())
                    .successCount(successEmails.size())
                    .failedCount(failedEmails.size())
                    .successEmails(successEmails)
                    .failedEmails(failedEmails)
                    .failedReasons(failedReasons)
                    .build();

            if (successEmails.isEmpty()) {
                return ResponseDto.fail("Failed to invite any members. Check reasons in response.");
            }

            String message = String.format("Invited %d/%d members successfully",
                    successEmails.size(), emails.size());
            return ResponseDto.success(response, message);

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * Remove member khỏi project
     */
    @Transactional
    public ResponseDto<String> removeMember(Integer projectId, Integer memberId, Authentication authentication) {
        try {
            Account currentUser = getCurrentAccount(authentication);

            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));

            if (!isOwner && !isAdmin) {
                return ResponseDto.fail("Only project owner or admin can remove members");
            }

            ProjectMember member = projectMemberRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("Member not found"));

            if (member.getAccount().getId().equals(project.getOwner().getId())) {
                return ResponseDto.fail("Cannot remove project owner");
            }

            if (!member.getProject().getId().equals(projectId)) {
                return ResponseDto.fail("Member does not belong to this project");
            }

            projectMemberRepository.delete(member);

            return ResponseDto.success(null, "Member removed successfully");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * Accept invitation từ email
     */
    @Transactional
    public ResponseDto<ProjectMemberResponse> acceptInvitationFromEmail(
            Integer invitationId,
            String token) {
        try {
            if (!verifyInvitationToken(invitationId, token)) {
                return ResponseDto.fail("Invalid or expired invitation link");
            }

            ProjectMember invitation = projectMemberRepository.findById(invitationId)
                    .orElseThrow(() -> new RuntimeException("Invitation not found"));

            if (!"Pending".equals(invitation.getStatus())) {
                return ResponseDto.fail("This invitation has already been processed");
            }

            if ("LECTURER".equalsIgnoreCase(invitation.getMemberRole())) {
                Optional<ProjectMember> existingLecturer = projectMemberRepository
                        .findLecturerByProjectId(invitation.getProject().getId());

                if (existingLecturer.isPresent()) {
                    return ResponseDto.fail("This project already has a lecturer mentor");
                }

            } else if ("STUDENT".equalsIgnoreCase(invitation.getMemberRole())) {
                long currentStudents = projectMemberRepository
                        .countByProjectIdAndMemberRoleAndStatus(
                                invitation.getProject().getId(),
                                "STUDENT",
                                "Approved"
                        );

                if (currentStudents >= MAX_STUDENTS_PER_PROJECT) {
                    return ResponseDto.fail("Maximum students reached");
                }
            }

            invitation.setStatus("Approved");
            projectMemberRepository.save(invitation);

            ProjectMemberResponse response = convertToResponse(invitation);

            return ResponseDto.success(response, "Invitation accepted successfully");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * Reject invitation từ email
     */
    @Transactional
    public ResponseDto<ProjectMemberResponse> rejectInvitationFromEmail(
            Integer invitationId,
            String token) {
        try {
            if (!verifyInvitationToken(invitationId, token)) {
                return ResponseDto.fail("Invalid or expired invitation link");
            }

            ProjectMember invitation = projectMemberRepository.findById(invitationId)
                    .orElseThrow(() -> new RuntimeException("Invitation not found"));

            if ("Cancelled".equals(invitation.getStatus())) {
                return ResponseDto.fail("This invitation has already been cancelled");
            }

            invitation.setStatus("Cancelled");
            projectMemberRepository.save(invitation);

            ProjectMemberResponse response = convertToResponse(invitation);

            return ResponseDto.success(response, "Invitation rejected successfully");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    private String generateInvitationToken(Integer invitationId) {
        String secretKey = "smrs-invitation-secret-key-2025";
        long timestamp = System.currentTimeMillis();
        String data = invitationId + ":" + secretKey + ":" + timestamp;

        return java.util.Base64.getEncoder()
                .encodeToString(data.getBytes());
    }

    private boolean verifyInvitationToken(Integer invitationId, String token) {
        try {
            String decoded = new String(java.util.Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");

            if (parts.length != 3) {
                return false;
            }

            Integer tokenInvitationId = Integer.parseInt(parts[0]);
            String tokenSecret = parts[1];
            long timestamp = Long.parseLong(parts[2]);

            if (!tokenInvitationId.equals(invitationId)) {
                return false;
            }

            if (!"smrs-invitation-secret-key-2025".equals(tokenSecret)) {
                return false;
            }

            long sevenDaysInMs = 7L * 24 * 60 * 60 * 1000;
            if (System.currentTimeMillis() - timestamp > sevenDaysInMs) {
                return false;
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private ProjectMemberResponse convertToResponse(ProjectMember member) {
        return ProjectMemberResponse.builder()
                .id(member.getId())
                .projectId(member.getProject().getId())
                .projectName(member.getProject().getName())
                .projectDescription(member.getProject().getDescription())
                .projectType(member.getProject().getType())
                .projectStatus(member.getProject().getStatus())
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