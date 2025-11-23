package com.example.smrsservice.controller;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.score.ProjectScoreCreateDto;
import com.example.smrsservice.dto.score.ProjectScoreResponseDto;
import com.example.smrsservice.dto.score.ProjectScoreUpdateDto;
import com.example.smrsservice.service.ProjectScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project-scores")
@RequiredArgsConstructor
public class ProjectScoreController {
    private final ProjectScoreService projectScoreService;

    @PostMapping
    public ResponseEntity<ResponseDto<ProjectScoreResponseDto>> createScore(
            @RequestBody ProjectScoreCreateDto dto,
            Authentication authentication) {
        ResponseDto<ProjectScoreResponseDto> result = projectScoreService.createScore(dto, authentication);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{scoreId}")
    public ResponseEntity<ResponseDto<ProjectScoreResponseDto>> updateScore(
            @PathVariable Integer scoreId,
            @RequestBody ProjectScoreUpdateDto dto,
            Authentication authentication) {
        ResponseDto<ProjectScoreResponseDto> result = projectScoreService.updateScore(scoreId, dto, authentication);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ResponseDto<List<ProjectScoreResponseDto>>> getScoresByProject(
            @PathVariable Integer projectId) {
        ResponseDto<List<ProjectScoreResponseDto>> result = projectScoreService.getScoresByProject(projectId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/report/{finalReportId}")
    public ResponseEntity<ResponseDto<List<ProjectScoreResponseDto>>> getScoresByFinalReport(
            @PathVariable Integer finalReportId) {
        ResponseDto<List<ProjectScoreResponseDto>> result = projectScoreService.getScoresByFinalReport(finalReportId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/project/{projectId}/average")
    public ResponseEntity<ResponseDto<Map<String, Object>>> getProjectAverageScore(
            @PathVariable Integer projectId) {
        ResponseDto<Map<String, Object>> result = projectScoreService.getProjectAverageScore(projectId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/report/{finalReportId}/average")
    public ResponseEntity<ResponseDto<Map<String, Object>>> getReportAverageScore(
            @PathVariable Integer finalReportId) {
        ResponseDto<Map<String, Object>> result = projectScoreService.getReportAverageScore(finalReportId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseDto<List<ProjectScoreResponseDto>>> searchScores(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        ResponseDto<List<ProjectScoreResponseDto>> result = projectScoreService.searchScores(keyword, status);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<ResponseDto<List<ProjectScoreResponseDto>>> getAllScores() {
        ResponseDto<List<ProjectScoreResponseDto>> result = projectScoreService.getAllScores();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my-scores")
    public ResponseEntity<ResponseDto<List<ProjectScoreResponseDto>>> getMyScores(
            Authentication authentication) {
        ResponseDto<List<ProjectScoreResponseDto>> result = projectScoreService.getScoresByLecturer(authentication);
        return ResponseEntity.ok(result);
    }
}
