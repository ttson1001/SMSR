package com.example.smrsservice.controller;

import com.example.smrsservice.dto.common.PaginatedResponseDto;
import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.stats.admin.ActivityDto;
import com.example.smrsservice.dto.stats.students.DeadlineDto;
import com.example.smrsservice.dto.stats.students.ProjectProgressDto;
import com.example.smrsservice.dto.stats.students.ScoreComparisonDto;
import com.example.smrsservice.dto.stats.students.StudentOverviewDto;
import com.example.smrsservice.service.StudentStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('Student')")  // ✅ ĐỔI SANG hasRole('Student')
public class StudentStatsController {

    private final StudentStatsService studentStatsService;

    @GetMapping("/overview")
    public ResponseEntity<ResponseDto<StudentOverviewDto>> getOverview(Authentication authentication) {
        StudentOverviewDto data = studentStatsService.getOverview(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/my-projects-status")
    public ResponseEntity<ResponseDto<Map<String, Long>>> getMyProjectsStatus(Authentication authentication) {
        Map<String, Long> data = studentStatsService.getMyProjectsStatus(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/my-roles")
    public ResponseEntity<ResponseDto<Map<String, Long>>> getMyRoles(Authentication authentication) {
        Map<String, Long> data = studentStatsService.getMyRoles(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/score-trend")
    public ResponseEntity<ResponseDto<Map<String, List<Double>>>> getScoreTrend(Authentication authentication) {
        Map<String, List<Double>> data = studentStatsService.getScoreTrend(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/projects-progress")
    public ResponseEntity<ResponseDto<List<ProjectProgressDto>>> getProjectsProgress(Authentication authentication) {
        List<ProjectProgressDto> data = studentStatsService.getProjectsProgress(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/recent-activities")
    public ResponseEntity<PaginatedResponseDto<List<ActivityDto>>> getRecentActivities(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int limit) {

        PaginatedResponseDto<List<ActivityDto>> response =
                studentStatsService.getRecentActivities(authentication, page, limit);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/score-comparison")
    public ResponseEntity<ResponseDto<ScoreComparisonDto>> getScoreComparison(Authentication authentication) {
        ScoreComparisonDto data = studentStatsService.getScoreComparison(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/upcoming-deadlines")
    public ResponseEntity<ResponseDto<List<DeadlineDto>>> getUpcomingDeadlines(Authentication authentication) {
        List<DeadlineDto> data = studentStatsService.getUpcomingDeadlines(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }
}