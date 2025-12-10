package com.example.smrsservice.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailResponse {
    private Integer id;
    private String email;
    private String avatar;
    private String phone;
    private String name;
    private Integer age;
    private String status;
    private RoleInfo  role;
    private Boolean  locked;


    private MajorInfo major;
    private CouncilManagerInfo councilManagerProfile;

    // ⭐ THÊM NESTED CLASSES (giống AccountDto)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo {
        private Integer id;
        private String roleName;
    }

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

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CouncilManagerInfo {
        private Integer profileId;
        private String employeeCode;
        private String positionTitle;
        private String department;
        private String status;
    }
}
