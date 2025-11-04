package com.example.smrsservice.controller;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.project.ProjectMemberResponse;
import com.example.smrsservice.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
     */
    @PutMapping("/invitations/{id}/approve")
    public ResponseEntity<ResponseDto<String>> approveInvitation(@PathVariable Integer id) {
        return ResponseEntity.ok(projectMemberService.approveInvitation(id));
    }

    /**
     * Từ chối lời mời
     */
    @PutMapping("/invitations/{id}/cancel")
    public ResponseEntity<ResponseDto<String>> cancelInvitation(@PathVariable Integer id) {
        return ResponseEntity.ok(projectMemberService.cancelInvitation(id));
    }

    /**
     * Lấy danh sách project đang tham gia
     */
    @GetMapping("/my-projects")
    public ResponseEntity<ResponseDto<List<ProjectMemberResponse>>> getMyProjects() {
        return ResponseEntity.ok(projectMemberService.getMyProjects());
    }

    /**
     * Lấy project đang active
     */
    @GetMapping("/my-active-project")
    public ResponseEntity<ResponseDto<ProjectMemberResponse>> getMyActiveProject() {
        return ResponseEntity.ok(projectMemberService.getMyActiveProject());
    }

    /**
     * Lấy danh sách thành viên của project
     */
    @GetMapping("/projects/{projectId}/members")
    public ResponseEntity<ResponseDto<List<ProjectMemberResponse>>> getProjectMembers(
            @PathVariable Integer projectId) {
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId));
    }
}
