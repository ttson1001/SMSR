package com.example.smrsservice.controller;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.milestone.MilestoneResponseDto;
import com.example.smrsservice.service.MilestoneFinalReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/final-milestones")
@RequiredArgsConstructor
public class MilestoneFinalReportController {
    private final MilestoneFinalReportService milestoneFinalReportService;

    @PutMapping("/{milestoneId}/review")
    public ResponseEntity<ResponseDto<MilestoneResponseDto>> reviewFinalReport(
            @PathVariable Integer milestoneId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String reviewComment,
            @RequestParam(required = false) Double progressPercent,
            Authentication authentication) {

        ResponseDto<MilestoneResponseDto> result =
                milestoneFinalReportService.reviewFinalReport(
                        milestoneId, status, reviewComment, progressPercent, authentication);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ResponseDto<MilestoneResponseDto>> getFinalReportByProject(
            @PathVariable Integer projectId) {

        ResponseDto<MilestoneResponseDto> result =
                milestoneFinalReportService.getFinalReportByProject(projectId);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<ResponseDto<List<MilestoneResponseDto>>> getAllFinalReports() {
        ResponseDto<List<MilestoneResponseDto>> result =
                milestoneFinalReportService.getAllFinalReports();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending")
    public ResponseEntity<ResponseDto<List<MilestoneResponseDto>>> getPendingFinalReports() {
        ResponseDto<List<MilestoneResponseDto>> result =
                milestoneFinalReportService.getPendingFinalReports();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my-reports")
    public ResponseEntity<ResponseDto<List<MilestoneResponseDto>>> getMyFinalReports(
            Authentication authentication) {

        ResponseDto<List<MilestoneResponseDto>> result =
                milestoneFinalReportService.getMyFinalReports(authentication);
        return ResponseEntity.ok(result);
    }
}
