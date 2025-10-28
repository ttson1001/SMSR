package com.example.smrsservice.dto.student;

import com.example.smrsservice.entity.Account;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentProfileUpdateResponseDto {
    Integer id;
    AccountSummary account;
    Integer schoolYear;
    String major;
    String currentClass;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountSummary {
        private Integer id;
        private String name;
        private String email;
        private String avatar;
        private String phone;
    }
}