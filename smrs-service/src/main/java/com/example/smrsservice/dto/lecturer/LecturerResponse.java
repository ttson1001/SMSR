package com.example.smrsservice.dto.lecturer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LecturerResponse {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String degree;
    private Integer yearsExperience;
    private Integer majorId;
    private String majorName;
}
