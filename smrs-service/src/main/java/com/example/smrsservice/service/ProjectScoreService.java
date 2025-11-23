package com.example.smrsservice.service;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.score.ProjectScoreCreateDto;
import com.example.smrsservice.dto.score.ProjectScoreResponseDto;
import com.example.smrsservice.dto.score.ProjectScoreUpdateDto;
import com.example.smrsservice.entity.Account;
import com.example.smrsservice.entity.FinalReport;
import com.example.smrsservice.entity.Project;
import com.example.smrsservice.entity.ProjectScore;
import com.example.smrsservice.repository.AccountRepository;
import com.example.smrsservice.repository.FinalReportRepository;
import com.example.smrsservice.repository.ProjectRepository;
import com.example.smrsservice.repository.ProjectScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectScoreService {
    private final ProjectScoreRepository projectScoreRepository;
    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;
    private final FinalReportRepository finalReportRepository;

    @Transactional
    public ResponseDto<ProjectScoreResponseDto> createScore(ProjectScoreCreateDto dto, Authentication authentication) {
        try {
            Account lecturer = currentAccount(authentication);

            if (!"LECTURER".equalsIgnoreCase(lecturer.getRole().getRoleName())) {
                return ResponseDto.fail("Only lecturers can score projects");
            }

            Project project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            FinalReport finalReport = finalReportRepository.findById(dto.getFinalReportId())
                    .orElseThrow(() -> new RuntimeException("Final report not found"));

            if (!finalReport.getProject().getId().equals(dto.getProjectId())) {
                return ResponseDto.fail("Report does not belong to this project");
            }

            if (projectScoreRepository.existsByFinalReportIdAndLecturerId(dto.getFinalReportId(), lecturer.getId())) {
                return ResponseDto.fail("You have already scored this report");
            }

            if (!validateScores(dto)) {
                return ResponseDto.fail("Invalid scores. Please check maximum points for each criteria");
            }

            ProjectScore score = ProjectScore.builder()
                    .project(project)
                    .finalReport(finalReport)
                    .lecturer(lecturer)
                    .criteria1Score(dto.getCriteria1Score())
                    .criteria2Score(dto.getCriteria2Score())
                    .criteria3Score(dto.getCriteria3Score())
                    .criteria4Score(dto.getCriteria4Score())
                    .criteria5Score(dto.getCriteria5Score())
                    .criteria6Score(dto.getCriteria6Score())
                    .bonusScore1(dto.getBonusScore1())
                    .bonusScore2(dto.getBonusScore2())
                    .comment(dto.getComment())
                    .build();

            projectScoreRepository.save(score);

            return ResponseDto.success(toResponseDto(score), "Score created successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    @Transactional
    public ResponseDto<ProjectScoreResponseDto> updateScore(Integer scoreId, ProjectScoreUpdateDto dto, Authentication authentication) {
        try {
            Account lecturer = currentAccount(authentication);

            ProjectScore score = projectScoreRepository.findById(scoreId)
                    .orElseThrow(() -> new RuntimeException("Score not found"));

            if (!score.getLecturer().getId().equals(lecturer.getId())) {
                return ResponseDto.fail("You can only update your own scores");
            }

            if (dto.getCriteria1Score() != null) score.setCriteria1Score(dto.getCriteria1Score());
            if (dto.getCriteria2Score() != null) score.setCriteria2Score(dto.getCriteria2Score());
            if (dto.getCriteria3Score() != null) score.setCriteria3Score(dto.getCriteria3Score());
            if (dto.getCriteria4Score() != null) score.setCriteria4Score(dto.getCriteria4Score());
            if (dto.getCriteria5Score() != null) score.setCriteria5Score(dto.getCriteria5Score());
            if (dto.getCriteria6Score() != null) score.setCriteria6Score(dto.getCriteria6Score());
            if (dto.getBonusScore1() != null) score.setBonusScore1(dto.getBonusScore1());
            if (dto.getBonusScore2() != null) score.setBonusScore2(dto.getBonusScore2());
            if (dto.getComment() != null) score.setComment(dto.getComment());

            projectScoreRepository.save(score);

            return ResponseDto.success(toResponseDto(score), "Score updated successfully");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<List<ProjectScoreResponseDto>> getScoresByProject(Integer projectId) {
        try {
            List<ProjectScore> scores = projectScoreRepository.findByProjectId(projectId);
            List<ProjectScoreResponseDto> result = scores.stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());
            return ResponseDto.success(result, "OK");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<List<ProjectScoreResponseDto>> getScoresByFinalReport(Integer finalReportId) {
        try {
            List<ProjectScore> scores = projectScoreRepository.findByFinalReportId(finalReportId);
            List<ProjectScoreResponseDto> result = scores.stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());
            return ResponseDto.success(result, "OK");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<Map<String, Object>> getProjectAverageScore(Integer projectId) {
        try {
            Double avgScore = projectScoreRepository.getAverageScoreByProjectId(projectId);
            List<ProjectScore> scores = projectScoreRepository.findByProjectId(projectId);

            Map<String, Object> result = new HashMap<>();
            result.put("averageScore", avgScore != null ? avgScore : 0.0);
            result.put("totalScores", scores.size());
            result.put("scores", scores.stream().map(this::toResponseDto).collect(Collectors.toList()));

            return ResponseDto.success(result, "OK");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<Map<String, Object>> getReportAverageScore(Integer finalReportId) {
        try {
            Double avgScore = projectScoreRepository.getAverageScoreByFinalReportId(finalReportId);
            List<ProjectScore> scores = projectScoreRepository.findByFinalReportId(finalReportId);

            Map<String, Object> result = new HashMap<>();
            result.put("averageScore", avgScore != null ? avgScore : 0.0);
            result.put("totalScores", scores.size());
            result.put("scores", scores.stream().map(this::toResponseDto).collect(Collectors.toList()));

            return ResponseDto.success(result, "OK");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<List<ProjectScoreResponseDto>> searchScores(String keyword, String status) {
        try {
            List<ProjectScore> scores = projectScoreRepository.findAll();

            // Filter by keyword (search in project name, lecturer name, comment)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String lowerKeyword = keyword.toLowerCase().trim();
                scores = scores.stream()
                        .filter(score ->
                                (score.getProject().getName() != null && score.getProject().getName().toLowerCase().contains(lowerKeyword)) ||
                                        (score.getLecturer().getName() != null && score.getLecturer().getName().toLowerCase().contains(lowerKeyword)) ||
                                        (score.getComment() != null && score.getComment().toLowerCase().contains(lowerKeyword))
                        )
                        .collect(Collectors.toList());
            }

            // Filter by status (based on final score range)
            if (status != null && !status.trim().isEmpty()) {
                scores = scores.stream()
                        .filter(score -> matchesStatus(score.getFinalScore(), status))
                        .collect(Collectors.toList());
            }

            List<ProjectScoreResponseDto> result = scores.stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());

            return ResponseDto.success(result, "Found " + result.size() + " scores");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<List<ProjectScoreResponseDto>> getAllScores() {
        try {
            List<ProjectScore> scores = projectScoreRepository.findAll();
            List<ProjectScoreResponseDto> result = scores.stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());
            return ResponseDto.success(result, "OK");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<List<ProjectScoreResponseDto>> getScoresByLecturer(Authentication authentication) {
        try {
            Account lecturer = currentAccount(authentication);
            List<ProjectScore> scores = projectScoreRepository.findByLecturerId(lecturer.getId());
            List<ProjectScoreResponseDto> result = scores.stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());
            return ResponseDto.success(result, "OK");
        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    private boolean matchesStatus(Double finalScore, String status) {
        if (finalScore == null) return false;

        switch (status.toUpperCase()) {
            case "EXCELLENT":
                return finalScore >= 90;
            case "GOOD":
                return finalScore >= 80 && finalScore < 90;
            case "AVERAGE":
                return finalScore >= 70 && finalScore < 80;
            case "BELOW_AVERAGE":
                return finalScore >= 60 && finalScore < 70;
            case "FAIL":
                return finalScore < 60;
            default:
                return true;
        }
    }

    private boolean validateScores(ProjectScoreCreateDto dto) {
        if (dto.getCriteria1Score() != null && (dto.getCriteria1Score() < 0 || dto.getCriteria1Score() > 10)) return false;
        if (dto.getCriteria2Score() != null && (dto.getCriteria2Score() < 0 || dto.getCriteria2Score() > 10)) return false;
        if (dto.getCriteria3Score() != null && (dto.getCriteria3Score() < 0 || dto.getCriteria3Score() > 15)) return false;
        if (dto.getCriteria4Score() != null && (dto.getCriteria4Score() < 0 || dto.getCriteria4Score() > 30)) return false;
        if (dto.getCriteria5Score() != null && (dto.getCriteria5Score() < 0 || dto.getCriteria5Score() > 15)) return false;
        if (dto.getCriteria6Score() != null && (dto.getCriteria6Score() < 0 || dto.getCriteria6Score() > 10)) return false;
        if (dto.getBonusScore1() != null && (dto.getBonusScore1() < 0 || dto.getBonusScore1() > 10)) return false;
        if (dto.getBonusScore2() != null && (dto.getBonusScore2() < 0 || dto.getBonusScore2() > 10)) return false;
        return true;
    }

    private ProjectScoreResponseDto toResponseDto(ProjectScore score) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return ProjectScoreResponseDto.builder()
                .id(score.getId())
                .projectId(score.getProject().getId())
                .projectName(score.getProject().getName())
                .finalReportId(score.getFinalReport().getId())
                .reportVersion(score.getFinalReport().getVersion())
                .lecturerId(score.getLecturer().getId())
                .lecturerName(score.getLecturer().getName())
                .criteria1Score(score.getCriteria1Score())
                .criteria2Score(score.getCriteria2Score())
                .criteria3Score(score.getCriteria3Score())
                .criteria4Score(score.getCriteria4Score())
                .criteria5Score(score.getCriteria5Score())
                .criteria6Score(score.getCriteria6Score())
                .bonusScore1(score.getBonusScore1())
                .bonusScore2(score.getBonusScore2())
                .totalScore(score.getTotalScore())
                .finalScore(score.getFinalScore())
                .comment(score.getComment())
                .scoreDate(score.getScoreDate() != null ? sdf.format(score.getScoreDate()) : null)
                .build();
    }

    private Account currentAccount(Authentication authentication) {
        if (authentication == null) throw new RuntimeException("User not authenticated");
        Object principal = authentication.getPrincipal();
        if (principal instanceof Account) return (Account) principal;
        if (principal instanceof String) {
            String email = (String) principal;
            return accountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Account not found with email: " + email));
        }
        throw new RuntimeException("Invalid authentication principal type");
    }
}
