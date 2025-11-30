package com.example.smrsservice.dto.stats.lecturer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorProjectDto {
    private Integer projectId;
    private String projectName;
    private Integer studentsCount;
    private String currentStatus;
    private String progress;
    private Double averageScore;
}
