package com.example.smrsservice.dto.councilmanagerprofile;

import com.example.smrsservice.common.CouncilManagerStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
@Builder
public class CouncilManagerProfileResponse {
    private Integer id;
    private Integer accountId;
    private String  accountEmail;

    private String employeeCode;
    private String department;
    private String positionTitle;
    private CouncilManagerStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String note;

    private Instant createdAt;
    private Instant updatedAt;
}
