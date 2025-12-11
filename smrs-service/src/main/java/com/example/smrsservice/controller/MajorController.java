package com.example.smrsservice.controller;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.lecturer.LecturerResponse;
import com.example.smrsservice.dto.major.MajorWithStatsDto;
import com.example.smrsservice.service.MajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/majors")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class MajorController {

    private final MajorService majorService;

    @GetMapping
    public ResponseDto<List<MajorWithStatsDto>> getAllMajors() {
        return majorService.getAllActiveMajors();
    }

    @GetMapping("/{majorId}/lecturers")
    public ResponseDto<List<LecturerResponse>> getLecturersByMajor(
            @PathVariable Integer majorId) {
        return majorService.getLecturersByMajor(majorId);
    }
}
