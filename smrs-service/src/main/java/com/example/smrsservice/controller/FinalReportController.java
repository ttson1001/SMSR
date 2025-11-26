package com.example.smrsservice.controller;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.report.FinalReportCreateDto;
import com.example.smrsservice.dto.report.FinalReportResponseDto;
import com.example.smrsservice.dto.report.FinalReportUpdateDto;
import com.example.smrsservice.service.FinalReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/final-reports")
@RequiredArgsConstructor
public class FinalReportController {
    private final FinalReportService finalReportService;

    @PostMapping
    public ResponseEntity<ResponseDto<FinalReportResponseDto>> submitReport(
            @RequestParam Integer projectId,
            @RequestParam String reportTitle,
            @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        FinalReportCreateDto dto = FinalReportCreateDto.builder()
                .projectId(projectId)
                .reportTitle(reportTitle)
                .description(description)
                .build();

        ResponseDto<FinalReportResponseDto> result = finalReportService.submitFinalReport(dto, file, authentication);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{reportId}")
    public ResponseEntity<ResponseDto<FinalReportResponseDto>> updateReportStatus(
            @PathVariable Integer reportId,
            @RequestBody FinalReportUpdateDto dto,
            Authentication authentication) {
        ResponseDto<FinalReportResponseDto> result = finalReportService.updateReportStatus(reportId, dto, authentication);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ResponseDto<List<FinalReportResponseDto>>> getReportsByProject(
            @PathVariable Integer projectId) {
        ResponseDto<List<FinalReportResponseDto>> result = finalReportService.getReportsByProject(projectId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/project/{projectId}/latest")
    public ResponseEntity<ResponseDto<FinalReportResponseDto>> getLatestReport(
            @PathVariable Integer projectId) {
        ResponseDto<FinalReportResponseDto> result = finalReportService.getLatestReport(projectId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending")
    public ResponseEntity<ResponseDto<List<FinalReportResponseDto>>> getPendingReports() {
        ResponseDto<List<FinalReportResponseDto>> result = finalReportService.getPendingReports();
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<ResponseDto<List<FinalReportResponseDto>>> getAllReports() {
        ResponseDto<List<FinalReportResponseDto>> result = finalReportService.getAllReports();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ResponseDto<List<FinalReportResponseDto>>> getReportsByStatus(
            @PathVariable String status) {
        ResponseDto<List<FinalReportResponseDto>> result = finalReportService.getReportsByStatus(status);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my-reports")
    public ResponseEntity<ResponseDto<List<FinalReportResponseDto>>> getMySubmittedReports(
            Authentication authentication) {
        ResponseDto<List<FinalReportResponseDto>> result = finalReportService.getMySubmittedReports(authentication);
        return ResponseEntity.ok(result);
    }


}