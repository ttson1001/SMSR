package com.example.smrsservice.controller;

import com.example.smrsservice.dto.common.PaginatedResponseDto;
import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.stats.admin.*;
import com.example.smrsservice.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")  // ✅ ĐỔI SANG hasAuthority
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/overview")
    public ResponseEntity<ResponseDto<AdminOverviewDto>> getOverview() {
        AdminOverviewDto data = adminStatsService.getOverview();
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/projects-by-status")
    public ResponseEntity<ResponseDto<Map<String, Long>>> getProjectsByStatus() {
        Map<String, Long> data = adminStatsService.getProjectsByStatus();
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/projects-timeline")
    public ResponseEntity<ResponseDto<ProjectsTimelineDto>> getProjectsTimeline(
            @RequestParam(defaultValue = "2024") int year,
            @RequestParam(defaultValue = "12") int months) {
        ProjectsTimelineDto data = adminStatsService.getProjectsTimeline(year, months);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/users-by-role")
    public ResponseEntity<ResponseDto<Map<String, Long>>> getUsersByRole() {
        Map<String, Long> data = adminStatsService.getUsersByRole();
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/top-users")
    public ResponseEntity<ResponseDto<List<TopUserDto>>> getTopUsers(
            @RequestParam(defaultValue = "10") int limit) {
        List<TopUserDto> data = adminStatsService.getTopUsers(limit);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/recent-activities")
    public ResponseEntity<PaginatedResponseDto<List<ActivityDto>>> getRecentActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        PaginatedResponseDto<List<ActivityDto>> response = adminStatsService.getRecentActivities(page, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/system-health")
    public ResponseEntity<ResponseDto<SystemHealthDto>> getSystemHealth() {
        SystemHealthDto data = adminStatsService.getSystemHealth();
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }
}