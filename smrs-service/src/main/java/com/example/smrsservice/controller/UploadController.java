package com.example.smrsservice.controller;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.service.UploadServic;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {
    private final UploadServic uploadService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = uploadService.uploadImage(file);
            return ResponseDto.success(imageUrl, "Upload ảnh thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi upload ảnh: " + e.getMessage());
        }
    }
}
