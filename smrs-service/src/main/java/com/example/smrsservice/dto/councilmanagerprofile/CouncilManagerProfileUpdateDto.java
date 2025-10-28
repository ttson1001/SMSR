package com.example.smrsservice.dto.councilmanagerprofile;

import com.example.smrsservice.common.CouncilManagerStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CouncilManagerProfileUpdateDto {
    private String employeeCode;              // optional
    private String department;                // optional
    private String positionTitle;             // optional
    private CouncilManagerStatus status;      // optional (ACTIVE/INACTIVE/SUSPENDED)
    private LocalDate startDate;              // optional
    private LocalDate endDate;                // optional
    private String note;
}
