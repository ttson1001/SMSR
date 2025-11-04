package com.example.smrsservice.dto.councilmanagerprofile;

import com.example.smrsservice.common.CouncilManagerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilManagerProfileDto {
    private Integer id;
    private Integer accountId;
    private String accountName;
    private String accountEmail;
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
