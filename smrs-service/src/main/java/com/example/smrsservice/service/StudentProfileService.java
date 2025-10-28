package com.example.smrsservice.service;

import com.example.smrsservice.dto.student.StudentProfileUpdateRequestDto;
import com.example.smrsservice.dto.student.StudentProfileUpdateResponseDto;
import com.example.smrsservice.entity.StudentProfile;
import com.example.smrsservice.repository.AccountRepository;
import com.example.smrsservice.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentProfileService {
    private final StudentProfileRepository studentProfileRepository;
    private final AccountRepository accountRepository;

    public StudentProfileUpdateResponseDto updateStudentProfile(Integer accountId,StudentProfileUpdateRequestDto request) {
        StudentProfile studentProfile = studentProfileRepository.findByAccount_Id(accountId)
                .orElseGet(() -> {
                    var account = accountRepository.findById(accountId)
                            .orElseThrow(() -> new RuntimeException("Account not found"));
                    var newProfile = new StudentProfile();
                    newProfile.setAccount(account);              // các field khác để null
                    return studentProfileRepository.save(newProfile);
                });


        if (request.getSchoolYear() != null) {
            studentProfile.setSchoolYear(request.getSchoolYear());
        }
        if (request.getMajor() != null && !request.getMajor().isBlank()) {
            studentProfile.setMajor(request.getMajor().trim());
        }
        if (request.getCurrentClass() != null && !request.getCurrentClass().isBlank()) {
            studentProfile.setCurrentClass(request.getCurrentClass().trim());
        }
        studentProfileRepository.save(studentProfile);


        var account = studentProfile.getAccount();
        return StudentProfileUpdateResponseDto.builder()
                .id(studentProfile.getId())
                .account(StudentProfileUpdateResponseDto.AccountSummary.builder()
                        .id(account.getId())
                        .name(account.getName())
                        .email(account.getEmail())
                        .avatar(account.getAvatar())
                        .phone(account.getPhone())
                        .build())
                .schoolYear(request.getSchoolYear())
                .major(request.getMajor())
                .currentClass(request.getCurrentClass())
                .build();


    }}
