package com.example.smrsservice.dto.concil;

import lombok.Data;

import java.util.List;

@Data
public class CreateCouncilRequest {
    private String councilCode;
    private String councilName;
    private String department;
    private String description;
    private List<String> lecturerEmails;
}
