package com.example.smrsservice.service;

import com.example.smrsservice.common.ProjectStatus;
import com.example.smrsservice.dto.common.PaginatedResponseDto;
import com.example.smrsservice.dto.stats.admin.ActivityDto;
import com.example.smrsservice.dto.stats.students.DeadlineDto;
import com.example.smrsservice.dto.stats.students.ProjectProgressDto;
import com.example.smrsservice.dto.stats.students.ScoreComparisonDto;
import com.example.smrsservice.dto.stats.students.StudentOverviewDto;
import com.example.smrsservice.entity.*;
import com.example.smrsservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentStatsService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectScoreRepository projectScoreRepository;
    private final MilestoneRepository milestoneRepository;
    private final AccountRepository accountRepository;

    /**
     * 1. Overview Cards
     */
    public StudentOverviewDto getOverview(Authentication authentication) {
        Account student = getCurrentAccount(authentication);

        Set<Integer> projectIds = getMyProjectIds(student.getId());
        long myProjects = projectIds.size();

        long projectsAsOwner = projectRepository.findByOwnerId(student.getId()).size();

        List<Project> allMyProjects = projectRepository.findAllById(projectIds);
        long completedProjects = allMyProjects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.COMPLETED)
                .count();

        Double averageScore = allMyProjects.stream()
                .map(p -> projectScoreRepository.getAverageScoreByProjectId(p.getId()))
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return StudentOverviewDto.builder()
                .myProjects(myProjects)
                .projectsAsOwner(projectsAsOwner)
                .completedProjects(completedProjects)
                .averageScore(averageScore)
                .projectsGrowth("+50%")
                .ownerGrowth("+100%")
                .completedGrowth("+0%")
                .scoreGrowth("+5%")
                .build();
    }

    /**
     * 2. My Projects Status
     */
    public Map<String, Long> getMyProjectsStatus(Authentication authentication) {
        Account student = getCurrentAccount(authentication);

        Set<Integer> projectIds = getMyProjectIds(student.getId());
        List<Project> myProjects = projectRepository.findAllById(projectIds);

        Map<String, Long> result = new LinkedHashMap<>();

        for (ProjectStatus status : ProjectStatus.values()) {
            long count = myProjects.stream()
                    .filter(p -> p.getStatus() == status)
                    .count();
            result.put(status.name(), count);
        }

        return result;
    }

    /**
     * 3. My Role Distribution
     */
    public Map<String, Long> getMyRoles(Authentication authentication) {
        Account student = getCurrentAccount(authentication);

        long asOwner = projectRepository.findByOwnerId(student.getId()).size();

        long asMember = projectMemberRepository.findByAccountId(student.getId()).stream()
                .filter(pm -> "STUDENT".equals(pm.getMemberRole()))
                .count();

        Map<String, Long> result = new LinkedHashMap<>();
        result.put("OWNER", asOwner);
        result.put("MEMBER", asMember);

        return result;
    }

    /**
     * 4. Score Trend
     */
    public Map<String, List<Double>> getScoreTrend(Authentication authentication) {
        Account student = getCurrentAccount(authentication);

        Set<Integer> projectIds = getMyProjectIds(student.getId());
        List<Project> myProjects = projectRepository.findAllById(projectIds).stream()
                .filter(p -> p.getStatus() == ProjectStatus.COMPLETED)
                .sorted(Comparator.comparing(Project::getCreateDate))
                .collect(Collectors.toList());

        List<Double> myScores = new ArrayList<>();
        List<Double> classAverage = new ArrayList<>();

        for (Project p : myProjects) {
            Double projectScore = projectScoreRepository.getAverageScoreByProjectId(p.getId());
            myScores.add(projectScore != null ? projectScore : 0.0);
            classAverage.add(7.5); // Mock
        }

        Map<String, List<Double>> result = new LinkedHashMap<>();
        result.put("myScores", myScores);
        result.put("classAverage", classAverage);

        return result;
    }

    /**
     * 5. Projects Progress
     */
    public List<ProjectProgressDto> getProjectsProgress(Authentication authentication) {
        Account student = getCurrentAccount(authentication);

        Set<Integer> projectIds = getMyProjectIds(student.getId());
        List<Project> myProjects = projectRepository.findAllById(projectIds);

        return myProjects.stream()
                .map(project -> {
                    String myRole = project.getOwner().getId().equals(student.getId())
                            ? "OWNER" : "MEMBER";

                    Integer daysLeft = null;
                    if (project.getDueDate() != null) {
                        LocalDateTime dueDate = LocalDateTime.ofInstant(
                                project.getDueDate().toInstant(), ZoneId.systemDefault());
                        LocalDateTime now = LocalDateTime.now();
                        daysLeft = (int) ChronoUnit.DAYS.between(now, dueDate);
                    }

                    Double currentScore = projectScoreRepository.getAverageScoreByProjectId(project.getId());
                    boolean hasScore = currentScore != null;

                    return ProjectProgressDto.builder()
                            .projectId(project.getId())
                            .projectName(project.getName())
                            .myRole(myRole)
                            .status(project.getStatus().toString())
                            .progress("75%")
                            .dueDate(project.getDueDate())
                            .daysLeft(daysLeft)
                            .hasScore(hasScore)
                            .currentScore(currentScore)
                            .build();
                })
                .sorted((a, b) -> {
                    if (a.getDaysLeft() == null) return 1;
                    if (b.getDaysLeft() == null) return -1;
                    return a.getDaysLeft().compareTo(b.getDaysLeft());
                })
                .collect(Collectors.toList());
    }

    /**
     * 6. Recent Activities - với Pagination
     */
    public PaginatedResponseDto<List<ActivityDto>> getRecentActivities(
            Authentication authentication, int page, int limit) {

        Account student = getCurrentAccount(authentication);

        Set<Integer> projectIds = getMyProjectIds(student.getId());
        List<Project> myProjects = projectRepository.findAllById(projectIds);

        List<ActivityDto> allActivities = new ArrayList<>();

        // Lấy tất cả activities
        for (Project p : myProjects) {
            List<ProjectScore> scores = projectScoreRepository.findByProjectId(p.getId());
            for (ProjectScore score : scores) {
                if (score.getScoreDate() != null) {
                    allActivities.add(ActivityDto.builder()
                            .type("PROJECT_SCORED")
                            .userId(score.getLecturer().getId())
                            .userName(score.getLecturer().getName())
                            .projectId(p.getId())
                            .projectName(p.getName())
                            .description("Scored " + score.getFinalScore())
                            .timestamp(score.getScoreDate().toInstant())
                            .icon("up")
                            .build());
                }
            }
        }

        // Sort by timestamp
        allActivities = allActivities.stream()
                .filter(a -> a.getTimestamp() != null)
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());

        // Pagination logic
        long totalElements = allActivities.size();
        int totalPages = (int) Math.ceil((double) totalElements / limit);
        int startIndex = page * limit;
        int endIndex = Math.min(startIndex + limit, (int) totalElements);

        List<ActivityDto> pagedActivities = (startIndex >= totalElements)
                ? new ArrayList<>()
                : allActivities.subList(startIndex, endIndex);

        PaginatedResponseDto.PaginationInfo pagination = PaginatedResponseDto.PaginationInfo.builder()
                .currentPage(page)
                .pageSize(limit)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();

        return PaginatedResponseDto.success(pagedActivities, pagination, "Success");
    }

    /**
     * 7. Score Comparison
     */
    public ScoreComparisonDto getScoreComparison(Authentication authentication) {
        Account student = getCurrentAccount(authentication);

        Set<Integer> projectIds = getMyProjectIds(student.getId());
        List<Project> myProjects = projectRepository.findAllById(projectIds);

        Double myAverageScore = myProjects.stream()
                .map(p -> projectScoreRepository.getAverageScoreByProjectId(p.getId()))
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return ScoreComparisonDto.builder()
                .myAverageScore(myAverageScore)
                .classAverageScore(7.5)
                .ranking(15)
                .totalStudents(120)
                .percentile("87%")
                .build();
    }

    /**
     * 8. Upcoming Deadlines
     */
    public List<DeadlineDto> getUpcomingDeadlines(Authentication authentication) {
        Account student = getCurrentAccount(authentication);

        Set<Integer> projectIds = getMyProjectIds(student.getId());
        List<Project> myProjects = projectRepository.findAllById(projectIds);

        List<DeadlineDto> deadlines = new ArrayList<>();

        for (Project p : myProjects) {
            List<Milestone> milestones = milestoneRepository.findByProjectId(p.getId());

            for (Milestone m : milestones) {
                if (m.getDueDate() != null) {
                    LocalDateTime dueDate = LocalDateTime.ofInstant(
                            m.getDueDate().toInstant(), ZoneId.systemDefault());
                    LocalDateTime now = LocalDateTime.now();
                    int daysLeft = (int) ChronoUnit.DAYS.between(now, dueDate);

                    String status = m.getReportSubmittedAt() != null
                            ? "SUBMITTED"
                            : (daysLeft < 0 ? "OVERDUE" : "NOT_SUBMITTED");

                    // ✅ SỬA: Dùng description hoặc tạo tên dựa vào isFinal
                    String milestoneName = m.getIsFinal() != null && m.getIsFinal()
                            ? "Final Report"
                            : (m.getDescription() != null && !m.getDescription().isEmpty()
                            ? m.getDescription()
                            : "Milestone #" + m.getId());

                    deadlines.add(DeadlineDto.builder()
                            .projectId(p.getId())
                            .projectName(p.getName())
                            .milestone(milestoneName)
                            .dueDate(m.getDueDate())
                            .daysLeft(daysLeft)
                            .status(status)
                            .build());
                }
            }
        }

        return deadlines.stream()
                .filter(d -> "NOT_SUBMITTED".equals(d.getStatus()) || "OVERDUE".equals(d.getStatus()))
                .sorted(Comparator.comparing(DeadlineDto::getDaysLeft))
                .collect(Collectors.toList());
    }

    private Set<Integer> getMyProjectIds(Integer userId) {
        Set<Integer> projectIds = new HashSet<>();

        List<Project> ownedProjects = projectRepository.findByOwnerId(userId);
        projectIds.addAll(ownedProjects.stream()
                .map(Project::getId)
                .collect(Collectors.toSet()));

        List<ProjectMember> memberProjects = projectMemberRepository.findByAccountId(userId);
        projectIds.addAll(memberProjects.stream()
                .map(pm -> pm.getProject().getId())
                .collect(Collectors.toSet()));

        return projectIds;
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
