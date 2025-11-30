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
public class DeadlineDto {
    private Integer projectId;
    private String projectName;
    private String milestone;
    private Date dueDate;
    private Integer daysLeft;
    private String status;
}
