package com.example.smrsservice.controller;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.service.UploadServic;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

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

    @PostMapping("/file")
    public ResponseEntity<ResponseDto<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseDto.fail("File is empty"));
            }

            String fileUrl = uploadService.uploadFile(file);
            return ResponseEntity.ok(ResponseDto.success(fileUrl, "File uploaded successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ResponseDto.fail("Upload failed: " + e.getMessage()));
        }
    }

    /**
     * ✅ Upload nhiều files
     */
    @PostMapping("/files")
    public ResponseEntity<ResponseDto<List<String>>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files) {
        try {
            List<String> urls = new ArrayList<>();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = uploadService.uploadFile(file);
                    urls.add(url);
                }
            }

            return ResponseEntity.ok(ResponseDto.success(urls, "Files uploaded successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ResponseDto.fail("Upload failed: " + e.getMessage()));
        }
    }

    /**
     * Upload auto
     */
    @PostMapping("/auto")
    public ResponseEntity<ResponseDto<String>> uploadAuto(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseDto.fail("File is empty"));
            }

            String url = uploadService.uploadAuto(file);
            return ResponseEntity.ok(ResponseDto.success(url, "File uploaded successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ResponseDto.fail("Upload failed: " + e.getMessage()));
        }
    }
}
