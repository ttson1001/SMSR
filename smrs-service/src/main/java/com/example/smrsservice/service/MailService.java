package com.example.smrsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendSimpleMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("baobao280799@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    /**
     * Gửi email mời tham gia project
     */
    public void sendProjectInvitation(String toEmail, String userName,
                                      String projectName, String ownerName, String role) {
        try {
            String roleText = "LECTURER".equalsIgnoreCase(role) ? "giảng viên hướng dẫn" : "thành viên";

            String subject = "Lời mời tham gia dự án: " + projectName;

            String body = String.format(
                    "Xin chào %s,\n\n" +
                            "Bạn đã được %s mời tham gia dự án '%s' với vai trò %s.\n\n" +
                            "Vui lòng đăng nhập vào hệ thống để chấp nhận hoặc từ chối lời mời này.\n\n" +
                            "Lưu ý:\n" +
                            "- Mỗi dự án chỉ có 1 giảng viên hướng dẫn\n" +
                            "- Mỗi dự án tối đa 5 sinh viên\n" +
                            "- Bạn chỉ được tham gia 1 dự án tại một thời điểm\n\n" +
                            "Trân trọng,\n" +
                            "SMRS Team",
                    userName, ownerName, projectName, roleText
            );

            sendSimpleMail(toEmail, subject, body);

        } catch (Exception e) {
            System.err.println("Failed to send invitation email: " + e.getMessage());
        }
    }
}

