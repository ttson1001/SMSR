package com.example.smrsservice.dto.stats.dean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LecturerActivityDto {
    private Integer lecturerId;
    private String lecturerName;
    private String lecturerEmail;
    private Integer councilsCount;
    private Integer projectsScored;
    private Double averageScoreGiven;
    private String activityLevel;
}
