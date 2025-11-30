package com.example.smrsservice.dto.stats.students;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreComparisonDto {
    private Double myAverageScore;
    private Double classAverageScore;
    private Integer ranking;
    private Integer totalStudents;
    private String percentile;
}