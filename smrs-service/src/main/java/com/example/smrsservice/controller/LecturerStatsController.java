package com.example.smrsservice.controller;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.stats.lecturer.CouncilStatsDto;
import com.example.smrsservice.dto.stats.lecturer.LecturerOverviewDto;
import com.example.smrsservice.dto.stats.lecturer.MentorProjectDto;
import com.example.smrsservice.dto.stats.lecturer.ScoringActivityDto;
import com.example.smrsservice.service.LecturerStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats/lecturer")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('Lecturer')")  // ✅ ĐỔI SANG hasAuthority - chú ý chữ L viết hoa
public class LecturerStatsController {

    private final LecturerStatsService lecturerStatsService;

    @GetMapping("/overview")
    public ResponseEntity<ResponseDto<LecturerOverviewDto>> getOverview(Authentication authentication) {
        LecturerOverviewDto data = lecturerStatsService.getOverview(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/mentor-projects-status")
    public ResponseEntity<ResponseDto<Map<String, Long>>> getMentorProjectsStatus(Authentication authentication) {
        Map<String, Long> data = lecturerStatsService.getMentorProjectsStatus(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/scoring-progress")
    public ResponseEntity<ResponseDto<Map<String, Object>>> getScoringProgress(Authentication authentication) {
        Map<String, Object> data = lecturerStatsService.getScoringProgress(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/mentor-projects-performance")
    public ResponseEntity<ResponseDto<List<MentorProjectDto>>> getMentorProjectsPerformance(
            Authentication authentication) {
        List<MentorProjectDto> data = lecturerStatsService.getMentorProjectsPerformance(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/my-councils-stats")
    public ResponseEntity<ResponseDto<List<CouncilStatsDto>>> getMyCouncilsStats(Authentication authentication) {
        List<CouncilStatsDto> data = lecturerStatsService.getMyCouncilsStats(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/recent-scores")
    public ResponseEntity<ResponseDto<List<ScoringActivityDto>>> getRecentScores(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit) {
        List<ScoringActivityDto> data = lecturerStatsService.getRecentScores(authentication, limit);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }

    @GetMapping("/score-distribution")
    public ResponseEntity<ResponseDto<Map<String, Long>>> getScoreDistribution(Authentication authentication) {
        Map<String, Long> data = lecturerStatsService.getScoreDistribution(authentication);
        return ResponseEntity.ok(ResponseDto.success(data, "Success"));
    }
}