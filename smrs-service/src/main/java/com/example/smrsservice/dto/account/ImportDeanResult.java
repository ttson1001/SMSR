package com.example.smrsservice.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportDeanResult {



    private Integer totalRows;
    private Integer successCount;
    private Integer failedCount;

    private List<String> successEmails;
    private List<String> failedEmails;
    private List<String> errors;

    private List<DeanDetail> successDeans;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeanDetail {
        private Integer accountId;
        private String email;
        private String name;
        private String phone;
        private Integer age;
        private String status;
        private String role;
        private Integer profileId;
        private String employeeCode;
        private String positionTitle;
        private String department;
        private Integer majorId;
        private String majorName;
        private String majorCode;
        private Boolean isNewAccount;
        private Boolean passwordGenerated;
        private String generatedPassword;
    }
}
