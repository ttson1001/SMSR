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
     * Lấy lời mời của user hiện tại
     */
    @GetMapping("/invitations")
    public ResponseEntity<ResponseDto<List<ProjectMemberResponse>>> getMyInvitations() {
        return ResponseEntity.ok(projectMemberService.getMyInvitations());
    }

    /**
     * Approve lời mời (từ app - cần login)
     */
    @PostMapping("/invitations/{id}/approve")
    public ResponseEntity<ResponseDto<ProjectMemberResponse>> approveInvitation(@PathVariable Integer id) {
        return ResponseEntity.ok(projectMemberService.approveInvitation(id));
    }

    /**
     * Cancel lời mời (từ app - cần login)
     */
    @PostMapping("/invitations/{id}/cancel")
    public ResponseEntity<ResponseDto<ProjectMemberResponse>> cancelInvitation(@PathVariable Integer id) {
        return ResponseEntity.ok(projectMemberService.cancelInvitation(id));
    }

    /**
     * ✅ Accept từ email (PUBLIC - không cần login)
     */
    @GetMapping("/accept/{invitationId}/{token}")
    public ResponseEntity<String> acceptInvitationFromEmail(
            @PathVariable Integer invitationId,
            @PathVariable String token) {

        ResponseDto<ProjectMemberResponse> result =
                projectMemberService.acceptInvitationFromEmail(invitationId, token);

        if (result.isSuccess()) {
            return ResponseEntity.ok(
                    "<!DOCTYPE html>" +
                            "<html lang='vi'>" +
                            "<head>" +
                            "    <meta charset='UTF-8'>" +
                            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                            "    <title>Chấp nhận lời mời thành công</title>" +
                            "    <style>" +
                            "        body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                            "               display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }" +
                            "        .container { background: white; padding: 40px; border-radius: 10px; box-shadow: 0 10px 30px rgba(0,0,0,0.2); " +
                            "                     text-align: center; max-width: 500px; }" +
                            "        .success-icon { font-size: 60px; color: #28a745; margin-bottom: 20px; }" +
                            "        h1 { color: #333; margin-bottom: 10px; }" +
                            "        p { color: #666; line-height: 1.6; margin: 15px 0; }" +
                            "        .project-name { color: #667eea; font-weight: bold; }" +
                            "        .button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; " +
                            "                  text-decoration: none; border-radius: 50px; margin-top: 20px; font-weight: 600; }" +
                            "        .button:hover { background: #5568d3; }" +
                            "    </style>" +
                            "</head>" +
                            "<body>" +
                            "    <div class='container'>" +
                            "        <div class='success-icon'>✅</div>" +
                            "        <h1>Chấp nhận lời mời thành công!</h1>" +
                            "        <p>Bạn đã tham gia dự án: <span class='project-name'>" + result.getData().getProjectName() + "</span></p>" +
                            "        <p>Vui lòng đăng nhập vào hệ thống để xem chi tiết dự án.</p>" +
                            "        <a href='http://localhost:3000/projects' class='button'>Xem dự án của bạn</a>" +
                            "    </div>" +
                            "</body>" +
                            "</html>"
            );
        } else {
            return ResponseEntity.badRequest().body(
                    "<!DOCTYPE html>" +
                            "<html lang='vi'>" +
                            "<head>" +
                            "    <meta charset='UTF-8'>" +
                            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                            "    <title>Lỗi</title>" +
                            "    <style>" +
                            "        body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); " +
                            "               display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }" +
                            "        .container { background: white; padding: 40px; border-radius: 10px; box-shadow: 0 10px 30px rgba(0,0,0,0.2); " +
                            "                     text-align: center; max-width: 500px; }" +
                            "        .error-icon { font-size: 60px; color: #dc3545; margin-bottom: 20px; }" +
                            "        h1 { color: #333; margin-bottom: 10px; }" +
                            "        p { color: #666; line-height: 1.6; }" +
                            "    </style>" +
                            "</head>" +
                            "<body>" +
                            "    <div class='container'>" +
                            "        <div class='error-icon'>❌</div>" +
                            "        <h1>Có lỗi xảy ra!</h1>" +
                            "        <p>" + result.getMessage() + "</p>" +
                            "    </div>" +
                            "</body>" +
                            "</html>"
            );
        }
    }

    /**
     * ✅ Reject từ email (PUBLIC - không cần login)
     */
    @GetMapping("/reject/{invitationId}/{token}")
    public ResponseEntity<String> rejectInvitationFromEmail(
            @PathVariable Integer invitationId,
            @PathVariable String token) {

        ResponseDto<ProjectMemberResponse> result =
                projectMemberService.rejectInvitationFromEmail(invitationId, token);

        if (result.isSuccess()) {
            return ResponseEntity.ok(
                    "<!DOCTYPE html>" +
                            "<html lang='vi'>" +
                            "<head>" +
                            "    <meta charset='UTF-8'>" +
                            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                            "    <title>Đã từ chối lời mời</title>" +
                            "    <style>" +
                            "        body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); " +
                            "               display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }" +
                            "        .container { background: white; padding: 40px; border-radius: 10px; box-shadow: 0 10px 30px rgba(0,0,0,0.2); " +
                            "                     text-align: center; max-width: 500px; }" +
                            "        .info-icon { font-size: 60px; color: #6c757d; margin-bottom: 20px; }" +
                            "        h1 { color: #333; margin-bottom: 10px; }" +
                            "        p { color: #666; line-height: 1.6; }" +
                            "        .project-name { font-weight: bold; }" +
                            "    </style>" +
                            "</head>" +
                            "<body>" +
                            "    <div class='container'>" +
                            "        <div class='info-icon'>ℹ️</div>" +
                            "        <h1>Đã từ chối lời mời</h1>" +
                            "        <p>Bạn đã từ chối tham gia dự án: <span class='project-name'>" + result.getData().getProjectName() + "</span></p>" +
                            "    </div>" +
                            "</body>" +
                            "</html>"
            );
        } else {
            return ResponseEntity.badRequest().body(
                    "<!DOCTYPE html>" +
                            "<html lang='vi'>" +
                            "<head>" +
                            "    <meta charset='UTF-8'>" +
                            "    <title>Lỗi</title>" +
                            "</head>" +
                            "<body>" +
                            "    <h2>❌ Có lỗi xảy ra!</h2>" +
                            "    <p>" + result.getMessage() + "</p>" +
                            "</body>" +
                            "</html>"
            );
        }
    }

    /**
     * Lấy projects của user
     */
    @GetMapping("/my-projects")
    public ResponseEntity<ResponseDto<List<ProjectMemberResponse>>> getMyProjects() {
        return ResponseEntity.ok(projectMemberService.getMyProjects());
    }

    /**
     * ✅ Lấy tất cả active projects của user
     */
    @GetMapping("/active-projects")
    public ResponseEntity<ResponseDto<List<ProjectMemberResponse>>> getMyActiveProjects() {
        return ResponseEntity.ok(projectMemberService.getMyActiveProjects());
    }

    /**
     * Lấy members của project
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ResponseDto<List<ProjectMemberResponse>>> getProjectMembers(
            @PathVariable Integer projectId) {
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId));
    }

    /**
     * Mời members vào project
     */
    @PostMapping("/project/{projectId}/invite")
    public ResponseEntity<ResponseDto<InviteMemberResponse>> inviteMembers(
            @PathVariable Integer projectId,
            @RequestBody InviteMemberRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                projectMemberService.inviteMembers(projectId, request.getEmails(), authentication)
        );
    }

    /**
     * Remove member khỏi project
     */
    @DeleteMapping("/project/{projectId}/member/{memberId}")
    public ResponseEntity<ResponseDto<String>> removeMember(
            @PathVariable Integer projectId,
            @PathVariable Integer memberId,
            Authentication authentication) {
        return ResponseEntity.ok(
                projectMemberService.removeMember(projectId, memberId, authentication)
        );
    }
}