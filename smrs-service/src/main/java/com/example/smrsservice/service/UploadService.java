package com.example.smrsservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.smrsservice.dto.upload.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", "smrs/images"
                    ));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload hình ảnh lên Cloudinary", e);
        }
    }


    public String uploadFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new IllegalArgumentException("File name is required");
            }

            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!isAllowedFileType(extension)) {
                throw new IllegalArgumentException(
                        "File type not allowed. Allowed: PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, TXT"
                );
            }

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", "smrs/documents",
                            "use_filename", true,
                            "unique_filename", true
                    ));

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload file lên Cloudinary", e);
        }
    }


    public FileUploadResponse uploadFileWithDetails(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new IllegalArgumentException("File name is required");
            }

            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!isAllowedFileType(extension)) {
                throw new IllegalArgumentException(
                        "File type not allowed. Allowed: PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, TXT"
                );
            }

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", "smrs/documents",
                            "use_filename", true,
                            "unique_filename", true
                    ));

            return FileUploadResponse.builder()
                    .url(uploadResult.get("secure_url").toString())
                    .fileName(originalFilename)
                    .fileType(extension)
                    .fileSize(file.getSize())
                    .cloudinaryId(uploadResult.get("public_id").toString())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload file lên Cloudinary", e);
        }
    }

    /**
     * ✅ Upload IMAGE với FileUploadResponse
     */
    public FileUploadResponse uploadImageWithDetails(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", "smrs/images"
                    ));

            return FileUploadResponse.builder()
                    .url(uploadResult.get("secure_url").toString())
                    .fileName(file.getOriginalFilename())
                    .fileType(getFileExtension(file.getOriginalFilename()))
                    .fileSize(file.getSize())
                    .cloudinaryId(uploadResult.get("public_id").toString())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload hình ảnh lên Cloudinary", e);
        }
    }

    /**
     * Upload tự động
     */
    public String uploadAuto(MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();

        if (isImageFile(extension)) {
            return uploadImage(file);
        }

        return uploadFile(file);
    }

    /**
     * Xóa file
     */
    public void deleteFile(String publicId, String resourceType) {
        try {
            cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType));
        } catch (IOException e) {
            throw new RuntimeException("Lỗi xóa file trên Cloudinary", e);
        }
    }

    // ==================== HELPER METHODS ====================

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private boolean isImageFile(String extension) {
        return extension.matches("(?i)(jpg|jpeg|png|gif|bmp|webp|svg)");
    }

    private boolean isAllowedFileType(String extension) {
        return extension.matches("(?i)(pdf|doc|docx|xls|xlsx|ppt|pptx|txt|csv)");
    }
}
