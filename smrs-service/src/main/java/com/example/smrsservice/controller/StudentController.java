package com.example.smrsservice.controller;

import com.example.smrsservice.dto.student.StudentProfileUpdateRequestDto;
import com.example.smrsservice.dto.student.StudentProfileUpdateResponseDto;
import com.example.smrsservice.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StudentController {
    private final StudentProfileService studentProfileService;

    @PatchMapping("/students/{accountId}/profile")
    public StudentProfileUpdateResponseDto updateProfileByAccountId(
            @PathVariable Integer accountId,
            @RequestBody StudentProfileUpdateRequestDto request) {
        return studentProfileService.updateStudentProfile(accountId, request);
    }
}

