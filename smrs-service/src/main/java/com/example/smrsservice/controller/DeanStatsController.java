package com.example.smrsservice.controller;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.stats.dean.CouncilPerformanceDto;
import com.example.smrsservice.dto.stats.dean.DeanOverviewDto;
import com.example.smrsservice.dto.stats.dean.LecturerActivityDto;
import com.example.smrsservice.dto.stats.dean.TimelineChartDto;
import com.example.smrsservice.service.DeanStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats/dean")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DEAN')")  // ✅ ĐỔI SANG hasRole
public class DeanStatsController {

    private final DeanStatsService deanStatsService;

    @GetMapping("/overview")
    public ResponseEntity<ResponseDto<DeanOverviewDto>> getOverview(Authentication authentication) {
        DeanOverviewDto data = deanStatsService.getOverview(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/projects-by-decision")
    public ResponseEntity<ResponseDto<Map<String, Long>>> getProjectsByDecision(Authentication authentication) {
        Map<String, Long> data = deanStatsService.getProjectsByDecision(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/councils-performance")
    public ResponseEntity<ResponseDto<List<CouncilPerformanceDto>>> getCouncilsPerformance(
            Authentication authentication) {
        List<CouncilPerformanceDto> data = deanStatsService.getCouncilsPerformance(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/decision-timeline")
    public ResponseEntity<ResponseDto<TimelineChartDto>> getDecisionTimeline(
            Authentication authentication,
            @RequestParam(defaultValue = "2024") int year,
            @RequestParam(defaultValue = "6") int months) {
        TimelineChartDto data = deanStatsService.getDecisionTimeline(authentication, year, months);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/lecturers-activity")
    public ResponseEntity<ResponseDto<List<LecturerActivityDto>>> getLecturersActivity(
            Authentication authentication) {
        List<LecturerActivityDto> data = deanStatsService.getLecturersActivity(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }
}