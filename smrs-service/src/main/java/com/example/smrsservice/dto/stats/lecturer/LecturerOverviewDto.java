package com.example.smrsservice.dto.stats.lecturer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LecturerOverviewDto {
    private Long projectsAsMentor;
    private Long projectsToScore;
    private Long projectsScored;
    private Long councilsJoined;

    private String mentorGrowth;
    private String scoreGrowth;
    private String scoredGrowth;
    private String councilsGrowth;
}
