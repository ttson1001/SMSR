package com.example.smrsservice.dto.student;

import lombok.Data;

@Data
public class StudentProfileUpdateRequestDto {
    Integer schoolYear;
    String major;
    String currentClass;

}
