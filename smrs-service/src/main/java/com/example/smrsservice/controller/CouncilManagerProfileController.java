//package com.example.smrsservice.controller;
//
//import com.example.smrsservice.dto.councilmanagerprofile.CouncilManagerProfileResponse;
//import com.example.smrsservice.dto.councilmanagerprofile.CouncilManagerProfileUpdateDto;
//import com.example.smrsservice.service.CouncilManagerProfileService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping
//@RequiredArgsConstructor
//public class CouncilManagerProfileController {
//    private final CouncilManagerProfileService councilManagerProfileService;
//
//    @PatchMapping("/{id}")
//    public ResponseEntity<CouncilManagerProfileResponse> updateById(
//            @PathVariable Integer id,
//            @RequestBody CouncilManagerProfileUpdateDto dto,
//            Authentication auth
//    ) {
//        return ResponseEntity.ok(councilManagerProfileService.updateById(id, dto, auth));
//    }
//
//    // Chủ sở hữu tự cập nhật
//    @PatchMapping("/me")
//    public ResponseEntity<CouncilManagerProfileResponse> updateMyProfile(
//            @RequestBody CouncilManagerProfileUpdateDto dto,
//            Authentication auth
//    ) {
//        return ResponseEntity.ok(councilManagerProfileService.updateMyProfile(dto, auth));
//    }
//}
