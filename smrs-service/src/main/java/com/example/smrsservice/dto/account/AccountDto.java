package com.example.smrsservice.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private Integer age;
    private String avatar;
    private String gender;
    private String address;
    private String role;

    // ⭐ THÊM 2 FIELDS CHO DEAN
    private MajorInfo major;
    private CouncilManagerInfo councilManagerProfile;

    // ⭐ THÊM NESTED CLASSES
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MajorInfo {
        private Integer id;
        private String name;
        private String code;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CouncilManagerInfo {
        private Integer profileId;
        private String employeeCode;
        private String positionTitle;
        private String department;
        private String status;
    }
}
