package com.example.smrsservice.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectImportDto {

    private String name;
    private String description;
    private String type;
    private Date dueDate;
    private String majorName;
    private String status;

    // Thông tin owner (optional - nếu không có thì dùng user hiện tại)
    private String ownerEmail;
}
