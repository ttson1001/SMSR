package com.example.smrsservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UploadServic {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload hình ảnh lên Cloudinary", e);
        }
    }

    public String uploadFile(MultipartFile file) {
        try {
            // Validate file
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new IllegalArgumentException("File name is required");
            }

            // Kiểm tra extension
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!isAllowedFileType(extension)) {
                throw new IllegalArgumentException(
                        "File type not allowed. Allowed: PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, TXT"
                );
            }

            // Upload với resource_type = "raw"
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",  // ✅ CHO PHÉP FILE
                            "folder", "smrs/documents",
                            "use_filename", true,
                            "unique_filename", true
                    ));

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload file lên Cloudinary", e);
        }
    }

    /**
     * ✅ THÊM: Upload tự động (image hoặc file)
     */
    public String uploadAuto(MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();

        if (isImageFile(extension)) {
            return uploadImage(file);
        }

        return uploadFile(file);
    }

    /**
     * ✅ THÊM: Xóa file
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
