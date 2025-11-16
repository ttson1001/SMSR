package com.example.smrsservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.smrsservice.dto.upload.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {
        try {
            String original = file.getOriginalFilename();

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", "smrs/images",
                            "public_id", original,
                            "overwrite", true
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload hình ảnh lên Cloudinary", e);
        }
    }

    public String uploadFile(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            if (filename == null)
                throw new IllegalArgumentException("File name is required");

            String extension = getExtension(filename);
            if (!isAllowedFileType(extension))
                throw new IllegalArgumentException("File type not allowed");


            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", "smrs/documents",
                            "public_id", filename,
                            "overwrite", true
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload file lên Cloudinary", e);
        }
    }

    public FileUploadResponse uploadFileWithDetails(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            if (filename == null)
                throw new IllegalArgumentException("File name is required");

            String extension = getExtension(filename);
            String resourceType = detectResourceType(extension);

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", resourceType,
                            "folder", "smrs/allfiles",
                            "public_id", filename,
                            "overwrite", true
                    )
            );

            return FileUploadResponse.builder()
                    .url(uploadResult.get("secure_url").toString())
                    .fileName(filename)
                    .fileType(extension)
                    .fileSize(file.getSize())
                    .cloudinaryId(uploadResult.get("public_id").toString())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload file lên Cloudinary", e);
        }
    }

    public List<FileUploadResponse> uploadMultipleFiles(List<MultipartFile> files) {
        return files.stream()
                .map(this::uploadFileWithDetails)
                .collect(Collectors.toList());
    }

    private String getExtension(String filename) {
        int index = filename.lastIndexOf(".");
        return index == -1 ? "" : filename.substring(index + 1).toLowerCase();
    }

    private boolean isAllowedFileType(String extension) {
        return extension.matches("(?i)(pdf|csv|txt|doc|docx|xls|xlsx|ppt|pptx)");
    }

    private boolean isImage(String ext) {
        return ext.matches("(?i)(jpg|jpeg|png|gif|bmp|webp|svg)");
    }

    private String detectResourceType(String ext) {
        if (isImage(ext)) return "image";
        if (List.of("mp4", "mov", "avi", "mkv", "mp3", "wav").contains(ext)) return "video";
        return "raw";
    }
}
