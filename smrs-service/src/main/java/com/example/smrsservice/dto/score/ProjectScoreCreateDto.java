package com.example.smrsservice.dto.score;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectScoreCreateDto {
    private Integer projectId;
    private Integer finalReportId;
    private Double criteria1Score;
    private Double criteria2Score;
    private Double criteria3Score;
    private Double criteria4Score;
    private Double criteria5Score;
    private Double criteria6Score;
    private Double bonusScore1;
    private Double bonusScore2;
    private String comment;
}
