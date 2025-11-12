package com.example.smrsservice.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Gửi email đơn giản (text)
     */
    public void sendSimpleMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    /**
     * ✅ Gửi email mời tham gia project với HTML + Buttons
     */
    public void sendProjectInvitation(
            String toEmail,
            String userName,
            String projectName,
            String ownerName,
            String role,
            Integer invitationId,
            String invitationToken) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Lời mời tham gia dự án: " + projectName);

            // Tạo links
            String acceptUrl = baseUrl + "/api/project-members/accept/" + invitationId + "/" + invitationToken;
            String rejectUrl = baseUrl + "/api/project-members/reject/" + invitationId + "/" + invitationToken;

            String roleText = "LECTURER".equalsIgnoreCase(role) ? "Giảng viên hướng dẫn" : "Thành viên sinh viên";

            // ✅ TEXT VERSION (fallback)
            String textContent = "Xin chào " + userName + ",\n\n" +
                    "Bạn đã được " + ownerName + " mời tham gia dự án '" + projectName + "' với vai trò " + roleText + ".\n\n" +
                    "Vui lòng click vào link dưới đây để chấp nhận:\n" +
                    acceptUrl + "\n\n" +
                    "Hoặc từ chối:\n" +
                    rejectUrl + "\n\n" +
                    "Lưu ý:\n" +
                    "- Mỗi dự án chỉ có 1 giảng viên hướng dẫn\n" +
                    "- Mỗi dự án tối đa 5 sinh viên\n" +
                    "- Bạn chỉ được tham gia 1 dự án tại một thời điểm\n\n" +
                    "Trân trọng,\n" +
                    "SMRS Team";

            // ✅ HTML VERSION
            String htmlContent =
                    "<!DOCTYPE html>" +
                            "<html>" +
                            "<head><meta charset='UTF-8'></head>" +
                            "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;'>" +

                            "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>" +

                            "<!-- Header -->" +
                            "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center;'>" +
                            "<h1 style='margin: 0; color: #ffffff; font-size: 24px;'>📨 Lời mời tham gia dự án</h1>" +
                            "</div>" +

                            "<!-- Body -->" +
                            "<div style='padding: 30px;'>" +

                            "<p style='color: #333333; font-size: 16px; margin-bottom: 15px;'>Xin chào <strong style='color: #667eea;'>" + userName + "</strong>,</p>" +

                            "<p style='color: #555555; font-size: 15px; margin-bottom: 15px;'><strong>" + ownerName + "</strong> đã mời bạn tham gia dự án:</p>" +

                            "<!-- Project Box -->" +
                            "<div style='background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 15px; margin: 20px 0; border-radius: 5px;'>" +
                            "<h2 style='margin: 0 0 10px 0; color: #333333; font-size: 18px;'>" + projectName + "</h2>" +
                            "<p style='margin: 0; color: #666666; font-size: 14px;'><strong>Vai trò:</strong> <span style='color: #667eea;'>" + roleText + "</span></p>" +
                            "</div>" +

                            "<p style='color: #555555; font-size: 15px; margin: 20px 0;'>Vui lòng click vào một trong các nút bên dưới:</p>" +

                            "<!-- Buttons -->" +
                            "<div style='text-align: center; margin: 30px 0;'>" +
                            "<a href='" + acceptUrl + "' style='display: inline-block; background-color: #28a745; color: #ffffff; padding: 12px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; margin: 0 5px;'>✅ Chấp nhận</a>" +
                            "<a href='" + rejectUrl + "' style='display: inline-block; background-color: #dc3545; color: #ffffff; padding: 12px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; margin: 0 5px;'>❌ Từ chối</a>" +
                            "</div>" +

                            "<!-- Info Box -->" +
                            "<div style='background-color: #fff3cd; border: 1px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 5px;'>" +
                            "<p style='margin: 0 0 10px 0; color: #856404; font-weight: bold;'>📌 Lưu ý:</p>" +
                            "<ul style='margin: 0; padding-left: 20px; color: #856404; font-size: 13px;'>" +
                            "<li>Mỗi dự án chỉ có 1 giảng viên hướng dẫn</li>" +
                            "<li>Mỗi dự án tối đa 5 sinh viên</li>" +
                            "<li>Bạn chỉ được tham gia 1 dự án tại một thời điểm</li>" +
                            "</ul>" +
                            "</div>" +

                            "</div>" +

                            "<!-- Footer -->" +
                            "<div style='background-color: #f8f9fa; padding: 15px; text-align: center; border-top: 1px solid #e9ecef;'>" +
                            "<p style='margin: 0; color: #6c757d; font-size: 12px;'>© 2025 SMRS Team</p>" +
                            "</div>" +

                            "</div>" +

                            "</body>" +
                            "</html>";

            // ✅ SET BOTH TEXT AND HTML
            helper.setText(textContent, htmlContent);

            mailSender.send(message);

            System.out.println("✅ Invitation email sent to: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ Gửi email thông báo kết quả
     */
    public void sendInvitationResult(String toEmail, String userName, String projectName, boolean accepted) {
        try {
            String subject = accepted
                    ? "Bạn đã tham gia dự án: " + projectName
                    : "Bạn đã từ chối lời mời dự án: " + projectName;

            String body = accepted
                    ? "Xin chào " + userName + ",\n\n" +
                    "Bạn đã chấp nhận tham gia dự án '" + projectName + "'.\n\n" +
                    "Vui lòng đăng nhập vào hệ thống để xem chi tiết dự án.\n\n" +
                    "Trân trọng,\nSMRS Team"
                    : "Xin chào " + userName + ",\n\n" +
                    "Bạn đã từ chối tham gia dự án '" + projectName + "'.\n\n" +
                    "Trân trọng,\nSMRS Team";

            sendSimpleMail(toEmail, subject, body);

        } catch (Exception e) {
            System.err.println("Failed to send result email: " + e.getMessage());
        }
    }
}