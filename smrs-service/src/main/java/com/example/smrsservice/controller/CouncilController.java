package com.example.smrsservice.controller;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.concil.CouncilResponse;
import com.example.smrsservice.dto.concil.CreateCouncilRequest;
import com.example.smrsservice.dto.concil.DeanDecisionRequest;
import com.example.smrsservice.dto.concil.ProjectCouncilDto;
import com.example.smrsservice.service.CouncilService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/councils")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class CouncilController {
    private final CouncilService councilService;

    /**
     * 1. Trưởng khoa tạo hội đồng
     * POST /api/councils
     */
    @PostMapping
    public ResponseEntity<ResponseDto<CouncilResponse>> createCouncil(
            @RequestBody CreateCouncilRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(councilService.createCouncil(request, authentication));
    }

    /**
     * 2. Gán project cho hội đồng
     * POST /api/councils/projects/{projectId}/assign
     */
    @PostMapping("/projects/{projectId}/assign")
    public ResponseEntity<ResponseDto<String>> assignProjectToCouncil(
            @PathVariable Integer projectId,
            @RequestBody AssignProjectRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(councilService.assignProjectToCouncil(
                projectId,
                request.getCouncilId(),
                authentication));
    }

    /**
     * 3. Trưởng khoa xem projects cần duyệt
     * GET /api/councils/my-pending-projects
     */
    @GetMapping("/my-pending-projects")
    public ResponseEntity<ResponseDto<List<ProjectCouncilDto>>> getMyPendingProjects(
            Authentication authentication) {
        return ResponseEntity.ok(councilService.getMyPendingProjects(authentication));
    }

    /**
     * 4. Trưởng khoa approve/reject project
     * POST /api/councils/projects/{projectId}/decision
     */
    @PostMapping("/projects/{projectId}/decision")
    public ResponseEntity<ResponseDto<String>> makeDecision(
            @PathVariable Integer projectId,
            @RequestBody DeanDecisionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(councilService.makeDecision(projectId, request, authentication));
    }

    /**
     * 5. Xem tất cả councils của trưởng khoa
     * GET /api/councils/my-councils
     */
    @GetMapping("/my-councils")
    public ResponseEntity<ResponseDto<List<CouncilResponse>>> getMyCouncils(
            Authentication authentication) {
        return ResponseEntity.ok(councilService.getMyCouncils(authentication));
    }

    /**
     * 6. Xem chi tiết một council
     * GET /api/councils/{councilId}
     */
    @GetMapping("/{councilId}")
    public ResponseEntity<ResponseDto<CouncilResponse>> getCouncilDetail(
            @PathVariable Integer councilId) {
        return ResponseEntity.ok(councilService.getCouncilDetail(councilId));
    }

    /**
     * 7. Xem tất cả projects của một council
     * GET /api/councils/{councilId}/projects
     */
    @GetMapping("/{councilId}/projects")
    public ResponseEntity<ResponseDto<List<ProjectCouncilDto>>> getProjectsByCouncil(
            @PathVariable Integer councilId) {
        return ResponseEntity.ok(councilService.getProjectsByCouncil(councilId));
    }

    // ==================== REQUEST DTOs ====================

    @Data
    public static class AssignProjectRequest {
        private Integer councilId;
    }

    @GetMapping("/my-joined-councils")
    public ResponseEntity<ResponseDto<List<CouncilResponse>>> getMyJoinedCouncils(
            Authentication authentication) {
        return ResponseEntity.ok(councilService.getMyJoinedCouncils(authentication));
    }
}
