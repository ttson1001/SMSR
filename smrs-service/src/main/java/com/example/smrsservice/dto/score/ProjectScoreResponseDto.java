package com.example.smrsservice.dto.score;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectScoreResponseDto {
    private Integer id;
    private Integer projectId;
    private String projectName;
    private Integer finalReportId;
    private Integer reportVersion;
    private Integer lecturerId;
    private String lecturerName;
    private Double criteria1Score;
    private Double criteria2Score;
    private Double criteria3Score;
    private Double criteria4Score;
    private Double criteria5Score;
    private Double criteria6Score;
    private Double bonusScore1;
    private Double bonusScore2;
    private Double totalScore;
    private Double finalScore;
    private String comment;
    private String scoreDate;
}
