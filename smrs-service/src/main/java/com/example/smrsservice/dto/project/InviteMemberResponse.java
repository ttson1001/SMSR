package com.example.smrsservice.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InviteMemberResponse {
    private int totalInvited;           // Tổng số người được mời
    private int successCount;           // Số người mời thành công
    private int failedCount;            // Số người mời thất bại
    private List<String> successEmails; // Danh sách email mời thành công
    private List<String> failedEmails;  // Danh sách email mời thất bại
    private List<String> failedReasons; // Lý do thất bại
}
