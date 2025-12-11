package com.example.smrsservice.dto.major;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LecturerByMajorDto {
    // Account info
    private Integer accountId;
    private String email;
    private String name;
    private String phone;
    private Integer age;
    private String avatar;
    private String status;

    // Lecturer profile info
    private Integer profileId;
    private String teachingMajor;
    private Integer yearsExperience;
    private String degree;
    private String employeeCode;
    private String department;
    private String position;
    private String expertise;

    // Major info
    private Integer majorId;
    private String majorName;
    private String majorCode;
}
