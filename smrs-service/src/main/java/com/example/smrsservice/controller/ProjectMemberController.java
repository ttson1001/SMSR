package com.example.smrsservice.controller;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.project.InviteMemberRequest;
import com.example.smrsservice.dto.project.InviteMemberResponse;
import com.example.smrsservice.dto.project.ProjectMemberResponse;
import com.example.smrsservice.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;


    /**
     * Lấy danh sách lời mời của user hiện tại
     * GET /api/project-members/invitations
     */
    @GetMapping("/invitations")
    public ResponseEntity<ResponseDto<List<ProjectMemberResponse>>> getMyInvitations() {
        return ResponseEntity.ok(projectMemberService.getMyInvitations());
    }

    /**
     * Chấp nhận lời mời
     * PUT /api/project-members/invitations/{id}/approve
     */
    @PutMapping("/invitations/{id}/approve")
    public ResponseEntity<ResponseDto<ProjectMemberResponse>> approveInvitation(@PathVariable Integer id) {
        return ResponseEntity.ok(projectMemberService.approveInvitation(id));
    }

    /**
     * Từ chối lời mời
     * PUT /api/project-members/invitations/{id}/cancel
     */
    @PutMapping("/invitations/{id}/cancel")
    public ResponseEntity<ResponseDto<ProjectMemberResponse>> cancelInvitation(@PathVariable Integer id) {
        return ResponseEntity.ok(projectMemberService.cancelInvitation(id));
    }

    /**
     * Lấy danh sách project đang tham gia
     * GET /api/project-members/my-projects
     */
    @GetMapping("/my-projects")
    public ResponseEntity<ResponseDto<List<ProjectMemberResponse>>> getMyProjects() {
        return ResponseEntity.ok(projectMemberService.getMyProjects());
    }

    /**
     * Lấy project đang active
     * GET /api/project-members/my-active-project
     */
    @GetMapping("/my-active-project")
    public ResponseEntity<ResponseDto<ProjectMemberResponse>> getMyActiveProject() {
        return ResponseEntity.ok(projectMemberService.getMyActiveProject());
    }

    /**
     * Lấy danh sách thành viên của project
     * GET /api/project-members/projects/{projectId}/members
     */
    @GetMapping("/projects/{projectId}/members")
    public ResponseEntity<ResponseDto<List<ProjectMemberResponse>>> getProjectMembers(
            @PathVariable Integer projectId) {
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId));
    }

    /**
     * Mời thêm members vào project
     * POST /api/project-members/projects/{projectId}/invite
     *
     * Body: {
     *   "emails": ["student1@example.com", "lecturer@example.com"]
     * }
     */
    @PostMapping("/projects/{projectId}/invite")
    public ResponseEntity<ResponseDto<InviteMemberResponse>> inviteMembers(
            @PathVariable Integer projectId,
            @RequestBody InviteMemberRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                projectMemberService.inviteMembers(projectId, request.getEmails(), authentication));
    }

    /**
     * Xóa member khỏi project (Owner/Admin only)
     * DELETE /api/project-members/projects/{projectId}/members/{memberId}
     */
    @DeleteMapping("/projects/{projectId}/members/{memberId}")
    public ResponseEntity<ResponseDto<String>> removeMember(
            @PathVariable Integer projectId,
            @PathVariable Integer memberId,
            Authentication authentication) {
        return ResponseEntity.ok(
                projectMemberService.removeMember(projectId, memberId, authentication));
    }
}
