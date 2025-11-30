package com.example.smrsservice.dto.stats.lecturer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringActivityDto {
    private Integer scoreId;
    private Integer projectId;
    private String projectName;
    private Double finalScore;
    private Instant scoreDate;
    private String councilName;
}
