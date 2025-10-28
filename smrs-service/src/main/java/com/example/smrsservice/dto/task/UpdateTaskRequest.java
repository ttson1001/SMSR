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
    @Nullable private String name;
    @Nullable private String description;
    @Nullable private Integer assignedToId;
    @Nullable private Integer milestoneId;   // -1 => bỏ gắn milestone
    @Nullable private Integer projectId;     // optional để validate đổi milestone

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Nullable private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Nullable private Date deadline;

    @DecimalMin("0.0") @DecimalMax("100.0")
    @Nullable private Double progressPercent;

    @Nullable private String status;
}
