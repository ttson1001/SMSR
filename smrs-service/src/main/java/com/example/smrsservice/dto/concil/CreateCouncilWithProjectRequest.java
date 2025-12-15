package com.example.smrsservice.dto.concil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCouncilWithProjectRequest {
    // Project to assign
    private Integer projectId;

    // Council info
    private String councilCode;
    private String councilName;
    private String department;
    private String description;

    // Lecturers to add as council members
    private List<String> lecturerEmails;
}