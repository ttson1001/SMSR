package com.example.smrsservice.dto.stats.students;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectProgressDto {
    private Integer projectId;
    private String projectName;
    private String myRole;
    private String status;
    private String progress;
    private Date dueDate;
    private Integer daysLeft;
    private Boolean hasScore;
    private Double currentScore;
}
