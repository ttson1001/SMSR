package com.example.smrsservice.service;

import com.example.smrsservice.common.ProjectStatus;
import com.example.smrsservice.dto.stats.lecturer.CouncilStatsDto;
import com.example.smrsservice.dto.stats.lecturer.LecturerOverviewDto;
import com.example.smrsservice.dto.stats.lecturer.MentorProjectDto;
import com.example.smrsservice.dto.stats.lecturer.ScoringActivityDto;
import com.example.smrsservice.entity.*;
import com.example.smrsservice.repository.AccountRepository;
import com.example.smrsservice.repository.CouncilMemberRepository;
import com.example.smrsservice.repository.ProjectMemberRepository;
import com.example.smrsservice.repository.ProjectScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LecturerStatsService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectScoreRepository projectScoreRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final AccountRepository accountRepository;

    /**
     * 1. Overview Cards
     */
    public LecturerOverviewDto getOverview(Authentication authentication) {
        Account lecturer = getCurrentAccount(authentication);

        List<ProjectMember> mentorships = projectMemberRepository.findByAccountIdAndMemberRole(
                lecturer.getId(), "LECTURER");
        long projectsAsMentor = mentorships.stream()
                .filter(pm -> "Approved".equals(pm.getStatus()))
                .count();

        long projectsScored = projectScoreRepository.findByLecturerId(lecturer.getId()).size();

        long councilsJoined = councilMemberRepository.findByLecturerId(lecturer.getId()).size();

        // Mock projects to score
        long projectsToScore = 5L;

        return LecturerOverviewDto.builder()
                .projectsAsMentor(projectsAsMentor)
                .projectsToScore(projectsToScore)
                .projectsScored(projectsScored)
                .councilsJoined(councilsJoined)
                .mentorGrowth("+12%")
                .scoreGrowth("+8%")
                .scoredGrowth("+20%")
                .councilsGrowth("+0%")
                .build();
    }

    /**
     * 2. Mentor Projects Status
     */
    public Map<String, Long> getMentorProjectsStatus(Authentication authentication) {
        Account lecturer = getCurrentAccount(authentication);

        List<ProjectMember> mentorships = projectMemberRepository.findByAccountIdAndMemberRole(
                lecturer.getId(), "LECTURER");

        Map<String, Long> result = new LinkedHashMap<>();

        for (ProjectStatus status : ProjectStatus.values()) {
            long count = mentorships.stream()
                    .filter(pm -> "Approved".equals(pm.getStatus()))
                    .map(ProjectMember::getProject)
                    .filter(p -> p.getStatus() == status)
                    .count();
            result.put(status.name(), count);
        }

        return result;
    }

    /**
     * 3. Scoring Progress
     */
    public Map<String, Object> getScoringProgress(Authentication authentication) {
        Account lecturer = getCurrentAccount(authentication);

        long scored = projectScoreRepository.findByLecturerId(lecturer.getId()).size();
        long toScore = 5L; // Mock
        long total = toScore + scored;

        String scoringRate = total > 0
                ? String.format("%.1f%%", (scored * 100.0 / total))
                : "0%";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("toScore", toScore);
        result.put("scored", scored);
        result.put("scoringRate", scoringRate);

        return result;
    }

    /**
     * 4. Mentor Projects Performance
     */
    public List<MentorProjectDto> getMentorProjectsPerformance(Authentication authentication) {
        Account lecturer = getCurrentAccount(authentication);

        List<ProjectMember> mentorships = projectMemberRepository.findByAccountIdAndMemberRole(
                lecturer.getId(), "LECTURER");

        return mentorships.stream()
                .filter(pm -> "Approved".equals(pm.getStatus()))
                .map(pm -> {
                    Project project = pm.getProject();

                    int studentsCount = (int) projectMemberRepository.findByProjectId(project.getId()).stream()
                            .filter(m -> "STUDENT".equals(m.getMemberRole()))
                            .count();

                    Double avgScore = projectScoreRepository.getAverageScoreByProjectId(project.getId());

                    return MentorProjectDto.builder()
                            .projectId(project.getId())
                            .projectName(project.getName())
                            .studentsCount(studentsCount)
                            .currentStatus(project.getStatus().toString())
                            .progress("75%")
                            .averageScore(avgScore != null ? avgScore : 0.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 5. My Councils Stats
     */
    public List<CouncilStatsDto> getMyCouncilsStats(Authentication authentication) {
        Account lecturer = getCurrentAccount(authentication);

        List<CouncilMember> myMemberships = councilMemberRepository.findByLecturerId(lecturer.getId());

        return myMemberships.stream()
                .map(cm -> CouncilStatsDto.builder()
                        .councilId(cm.getCouncil().getId())
                        .councilName(cm.getCouncil().getCouncilName())
                        .councilCode(cm.getCouncil().getCouncilCode())
                        .myRole(cm.getRole())
                        .totalProjects(10) // Mock
                        .projectsScored(5) // Mock
                        .projectsToScore(5) // Mock
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 6. Recent Scoring Activities
     */
    public List<ScoringActivityDto> getRecentScores(Authentication authentication, int limit) {
        Account lecturer = getCurrentAccount(authentication);

        return projectScoreRepository.findByLecturerId(lecturer.getId()).stream()
                .sorted((a, b) -> b.getScoreDate().compareTo(a.getScoreDate()))
                .limit(limit)
                .map(score -> ScoringActivityDto.builder()
                        .scoreId(score.getId())
                        .projectId(score.getProject().getId())
                        .projectName(score.getProject().getName())
                        .finalScore(score.getFinalScore())
                        .scoreDate(score.getScoreDate().toInstant())
                        .councilName("Council Name")
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 7. Score Distribution
     */
    public Map<String, Long> getScoreDistribution(Authentication authentication) {
        Account lecturer = getCurrentAccount(authentication);

        List<ProjectScore> scores = projectScoreRepository.findByLecturerId(lecturer.getId());

        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("0-4", 0L);
        distribution.put("4-5", 0L);
        distribution.put("5-6", 0L);
        distribution.put("6-7", 0L);
        distribution.put("7-8", 0L);
        distribution.put("8-9", 0L);
        distribution.put("9-10", 0L);

        for (ProjectScore score : scores) {
            Double finalScore = score.getFinalScore();
            if (finalScore == null) continue;

            if (finalScore < 4) distribution.put("0-4", distribution.get("0-4") + 1);
            else if (finalScore < 5) distribution.put("4-5", distribution.get("4-5") + 1);
            else if (finalScore < 6) distribution.put("5-6", distribution.get("5-6") + 1);
            else if (finalScore < 7) distribution.put("6-7", distribution.get("6-7") + 1);
            else if (finalScore < 8) distribution.put("7-8", distribution.get("7-8") + 1);
            else if (finalScore < 9) distribution.put("8-9", distribution.get("8-9") + 1);
            else distribution.put("9-10", distribution.get("9-10") + 1);
        }

        return distribution;
    }

    private Account getCurrentAccount(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Account) {
            return (Account) principal;
        }

        if (principal instanceof String) {
            String email = (String) principal;
            return accountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Account not found"));
        }

        throw new RuntimeException("Invalid authentication principal");
    }
}
