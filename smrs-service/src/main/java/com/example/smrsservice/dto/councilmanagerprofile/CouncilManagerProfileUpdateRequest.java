package com.example.smrsservice.dto.councilmanagerprofile;

import com.example.smrsservice.common.CouncilManagerStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CouncilManagerProfileUpdateRequest {
    private String employeeCode;
    private String councilName;
    private String councilCode;
    private String department;
    private String positionTitle;
    private CouncilManagerStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String note;
}
