package com.example.smrsservice.service;

import com.example.smrsservice.common.DecisionStatus;
import com.example.smrsservice.common.ProjectStatus;
import com.example.smrsservice.dto.stats.dean.CouncilPerformanceDto;
import com.example.smrsservice.dto.stats.dean.DeanOverviewDto;
import com.example.smrsservice.dto.stats.dean.LecturerActivityDto;
import com.example.smrsservice.dto.stats.dean.TimelineChartDto;
import com.example.smrsservice.entity.*;
import com.example.smrsservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeanStatsService {

    private final CouncilRepository councilRepository;
    private final CouncilManagerProfileRepository councilProfileRepository;
    private final ProjectCouncilRepository projectCouncilRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final ProjectScoreRepository projectScoreRepository;
    private final AccountRepository accountRepository;

    /**
     * 1. Overview Cards
     */
    public DeanOverviewDto getOverview(Authentication authentication) {
        Account dean = getCurrentAccount(authentication);
        CouncilManagerProfile deanProfile = councilProfileRepository
                .findByAccountId(dean.getId())
                .orElseThrow(() -> new RuntimeException("Dean profile not found"));

        List<Council> myCouncils = councilRepository.findByDeanId(deanProfile.getId());
        long totalCouncils = myCouncils.size();

        long pendingProjects = projectCouncilRepository.findPendingProjectsByDean(deanProfile.getId()).size();

        long approvedProjects = myCouncils.stream()
                .flatMap(c -> projectCouncilRepository.findByCouncilId(c.getId()).stream())
                .filter(pc -> pc.getDecision() == DecisionStatus.APPROVED)
                .count();

        long totalLecturers = myCouncils.stream()
                .flatMap(c -> councilMemberRepository.findByCouncilId(c.getId()).stream())
                .map(cm -> cm.getLecturer().getId())
                .distinct()
                .count();

        return DeanOverviewDto.builder()
                .totalCouncils(totalCouncils)
                .pendingProjects(pendingProjects)
                .approvedProjects(approvedProjects)
                .totalLecturers(totalLecturers)
                .councilsGrowth("+20%")
                .pendingGrowth("+5%")
                .approvedGrowth("+15%")
                .lecturersGrowth("+10%")
                .build();
    }

    /**
     * 2. Projects by Decision Status
     */
    public Map<String, Long> getProjectsByDecision(Authentication authentication) {
        Account dean = getCurrentAccount(authentication);
        CouncilManagerProfile deanProfile = councilProfileRepository
                .findByAccountId(dean.getId())
                .orElseThrow(() -> new RuntimeException("Dean profile not found"));

        List<Council> myCouncils = councilRepository.findByDeanId(deanProfile.getId());
        List<ProjectCouncil> allProjects = myCouncils.stream()
                .flatMap(c -> projectCouncilRepository.findByCouncilId(c.getId()).stream())
                .collect(Collectors.toList());

        Map<String, Long> result = new LinkedHashMap<>();
        result.put("PENDING", allProjects.stream()
                .filter(pc -> pc.getDecision() == DecisionStatus.PENDING).count());
        result.put("APPROVED", allProjects.stream()
                .filter(pc -> pc.getDecision() == DecisionStatus.APPROVED).count());
        result.put("REJECTED", allProjects.stream()
                .filter(pc -> pc.getDecision() == DecisionStatus.REJECTED).count());

        return result;
    }

    /**
     * 3. Councils Performance
     */
    public List<CouncilPerformanceDto> getCouncilsPerformance(Authentication authentication) {
        Account dean = getCurrentAccount(authentication);
        CouncilManagerProfile deanProfile = councilProfileRepository
                .findByAccountId(dean.getId())
                .orElseThrow(() -> new RuntimeException("Dean profile not found"));

        List<Council> myCouncils = councilRepository.findByDeanId(deanProfile.getId());

        return myCouncils.stream()
                .map(council -> {
                    List<ProjectCouncil> projects = projectCouncilRepository.findByCouncilId(council.getId());
                    int totalProjects = projects.size();

                    long completedProjects = projects.stream()
                            .filter(pc -> pc.getProject().getStatus() == ProjectStatus.COMPLETED)
                            .count();

                    Double avgScore = projects.stream()
                            .map(pc -> projectScoreRepository.getAverageScoreByProjectId(pc.getProject().getId()))
                            .filter(Objects::nonNull)
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);

                    int activeMembers = councilMemberRepository.findByCouncilId(council.getId()).size();

                    String completionRate = totalProjects > 0
                            ? String.format("%.1f%%", (completedProjects * 100.0 / totalProjects))
                            : "0%";

                    return CouncilPerformanceDto.builder()
                            .councilId(council.getId())
                            .councilName(council.getCouncilName())
                            .councilCode(council.getCouncilCode())
                            .totalProjects(totalProjects)
                            .completedProjects((int) completedProjects)
                            .averageScore(avgScore)
                            .activeMembers(activeMembers)
                            .completionRate(completionRate)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 4. Decision Timeline
     */
    public TimelineChartDto getDecisionTimeline(Authentication authentication, int year, int months) {
        Account dean = getCurrentAccount(authentication);
        CouncilManagerProfile deanProfile = councilProfileRepository
                .findByAccountId(dean.getId())
                .orElseThrow(() -> new RuntimeException("Dean profile not found"));

        List<Council> myCouncils = councilRepository.findByDeanId(deanProfile.getId());
        List<ProjectCouncil> allProjects = myCouncils.stream()
                .flatMap(c -> projectCouncilRepository.findByCouncilId(c.getId()).stream())
                .collect(Collectors.toList());

        List<String> labels = new ArrayList<>();
        List<Long> approved = new ArrayList<>();
        List<Long> rejected = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.from(now.minusMonths(i));
            labels.add(yearMonth.getMonth().toString().substring(0, 3));

            LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            long approvedCount = allProjects.stream()
                    .filter(pc -> pc.getDecision() == DecisionStatus.APPROVED)
                    .filter(pc -> pc.getDecisionDate() != null)
                    .filter(pc -> {
                        LocalDateTime decisionTime = LocalDateTime.ofInstant(
                                pc.getDecisionDate(), ZoneId.systemDefault());
                        return !decisionTime.isBefore(start) && !decisionTime.isAfter(end);
                    })
                    .count();

            long rejectedCount = allProjects.stream()
                    .filter(pc -> pc.getDecision() == DecisionStatus.REJECTED)
                    .filter(pc -> pc.getDecisionDate() != null)
                    .filter(pc -> {
                        LocalDateTime decisionTime = LocalDateTime.ofInstant(
                                pc.getDecisionDate(), ZoneId.systemDefault());
                        return !decisionTime.isBefore(start) && !decisionTime.isAfter(end);
                    })
                    .count();

            approved.add(approvedCount);
            rejected.add(rejectedCount);
        }

        return TimelineChartDto.builder()
                .labels(labels)
                .dataset1(approved)
                .dataset2(rejected)
                .dataset1Label("Approved")
                .dataset2Label("Rejected")
                .build();
    }

    /**
     * 5. Lecturers Activity
     */
    public List<LecturerActivityDto> getLecturersActivity(Authentication authentication) {
        Account dean = getCurrentAccount(authentication);
        CouncilManagerProfile deanProfile = councilProfileRepository
                .findByAccountId(dean.getId())
                .orElseThrow(() -> new RuntimeException("Dean profile not found"));

        List<Council> myCouncils = councilRepository.findByDeanId(deanProfile.getId());

        Map<Integer, Account> lecturerMap = new HashMap<>();
        Map<Integer, Integer> councilsCountMap = new HashMap<>();

        for (Council council : myCouncils) {
            List<CouncilMember> members = councilMemberRepository.findByCouncilId(council.getId());
            for (CouncilMember cm : members) {
                Account lecturer = cm.getLecturer();
                lecturerMap.put(lecturer.getId(), lecturer);
                councilsCountMap.put(lecturer.getId(),
                        councilsCountMap.getOrDefault(lecturer.getId(), 0) + 1);
            }
        }

        return lecturerMap.values().stream()
                .map(lecturer -> {
                    int councilsCount = councilsCountMap.getOrDefault(lecturer.getId(), 0);

                    int projectsScored = projectScoreRepository.findByLecturerId(lecturer.getId()).size();

                    Double avgScoreGiven = projectScoreRepository.findByLecturerId(lecturer.getId()).stream()
                            .map(ProjectScore::getFinalScore)
                            .filter(Objects::nonNull)
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);

                    String activityLevel = projectsScored >= 10 ? "HIGH"
                            : projectsScored >= 5 ? "MEDIUM" : "LOW";

                    return LecturerActivityDto.builder()
                            .lecturerId(lecturer.getId())
                            .lecturerName(lecturer.getName())
                            .lecturerEmail(lecturer.getEmail())
                            .councilsCount(councilsCount)
                            .projectsScored(projectsScored)
                            .averageScoreGiven(avgScoreGiven)
                            .activityLevel(activityLevel)
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getProjectsScored(), a.getProjectsScored()))
                .collect(Collectors.toList());
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
    /// //
}
