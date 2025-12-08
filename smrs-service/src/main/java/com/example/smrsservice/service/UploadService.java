package com.example.smrsservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.smrsservice.dto.upload.FileUploadResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
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

    public String uploadFileToNode(MultipartFile file, String folder) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Không có file để upload.");
        }

        long maxSize = 100L * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("Dung lượng file vượt quá giới hạn 100MB.");
        }

        List<String> allowedExtensions = List.of(
                ".pdf", ".doc", ".docx", ".xls", ".xlsx",
                ".zip", ".png", ".jpg", ".jpeg"
        );

        String filename = file.getOriginalFilename();
        String ext = filename.substring(filename.lastIndexOf(".")).toLowerCase();

        if (!allowedExtensions.contains(ext)) {
            throw new RuntimeException("Định dạng file " + ext + " không được hỗ trợ.");
        }

        try {
            String uploadApiUrl = "http://103.200.20.45:3030/upload/" + folder;

            OkHttpClient client = new OkHttpClient();

            // Multipart form data
            RequestBody fileBody = RequestBody.create(
                    file.getBytes(),
                    MediaType.parse(file.getContentType()) // lấy đúng content type của file
            );

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", filename, fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(uploadApiUrl)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new RuntimeException("Upload thất bại: " + response.code());
            }

            String resultJson = response.body().string();

            JsonObject json = JsonParser.parseString(resultJson).getAsJsonObject();
            String url = json.getAsJsonObject("file").get("url").getAsString();

            return url;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload file: " + e.getMessage(), e);
        }
    }
}
