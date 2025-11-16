package com.example.smrsservice.controller;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.upload.FileUploadResponse;
import com.example.smrsservice.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {
    private final UploadService uploadService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = uploadService.uploadImage(file);
            return ResponseDto.success(imageUrl, "Upload ảnh thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @PostMapping( value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple files")
    public ResponseEntity<ResponseDto<List<FileUploadResponse>>> uploadMultiple(
            @Parameter(
                    description = "Select multiple files",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
                    )
            )
            @RequestPart("files") List<MultipartFile> files) {

        try {
            List<FileUploadResponse> results = uploadService.uploadMultipleFiles(files);
            return ResponseEntity.ok(
                    ResponseDto.success(results, "Files uploaded successfully")
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ResponseDto.fail("Upload failed: " + e.getMessage()));
        }
    }

    @PostMapping(value ="/auto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
