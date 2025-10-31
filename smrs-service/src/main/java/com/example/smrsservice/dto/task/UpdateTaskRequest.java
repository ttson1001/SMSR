package com.example.smrsservice.dto.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskRequest {
    private String name;
    private String description;
    private Integer assignedToId;
    private Integer milestoneId;
    private Date deadline;
    private Double progressPercent;
    private String status;  // Pending / InProgress / Done ...
}
