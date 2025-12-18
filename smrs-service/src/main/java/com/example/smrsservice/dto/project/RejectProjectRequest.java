package com.example.smrsservice.dto.project;

import com.example.smrsservice.common.RejectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectProjectRequest {

    @NotBlank(message = "Reason is required")
    private String reason;

    private String feedback;

    @NotNull(message = "Reject type is required")
    private RejectType rejectType;

    private Integer revisionDays;
}
