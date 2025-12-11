// ============= MajorService.java - CLEAN VERSION =============
// Đường dẫn: src/main/java/com/example/smrsservice/service/MajorService.java

package com.example.smrsservice.service;

import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.dto.lecturer.LecturerResponse;
import com.example.smrsservice.dto.major.MajorWithStatsDto;
import com.example.smrsservice.entity.LecturerProfile;
import com.example.smrsservice.entity.Major;
import com.example.smrsservice.repository.LecturerProfileRepository;
import com.example.smrsservice.repository.MajorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MajorService {

    private final MajorRepository majorRepository;
    private final LecturerProfileRepository lecturerProfileRepository;

    /**
     * GET /api/majors
     * Trả về tất cả majors với lecturer count
     */
    public ResponseDto<List<MajorWithStatsDto>> getAllActiveMajors() {
        try {
            List<Major> majors = majorRepository.findAllActiveMajors();

            List<MajorWithStatsDto> responses = majors.stream()
                    .map(major -> {
                        // ⭐ COUNT số lecturer
                        int lecturerCount = lecturerProfileRepository.countByMajorId(major.getId());

                        return MajorWithStatsDto.builder()
                                .id(major.getId())
                                .name(major.getName())
                                .code(major.getCode())
                                .description(major.getDescription())
                                .isActive(major.getIsActive())
                                .lecturerCount(lecturerCount)
                                .build();
                    })
                    .collect(Collectors.toList());

            return ResponseDto.success(responses, "Get majors successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * GET /api/majors/{majorId}/lecturers
     * Lấy danh sách giảng viên theo khoa
     */
    public ResponseDto<List<LecturerResponse>> getLecturersByMajor(Integer majorId) {
        try {
            // ⭐ Dùng Integer trực tiếp (KHÔNG dùng Long.valueOf)
            Major major = majorRepository.findById(majorId)
                    .orElseThrow(() -> new RuntimeException("Major not found"));

            List<LecturerProfile> profiles = lecturerProfileRepository.findByMajorId(majorId);

            List<LecturerResponse> responses = profiles.stream()
                    .filter(p -> p.getAccount() != null)
                    .map(p -> LecturerResponse.builder()
                            .id(p.getAccount().getId())
                            .name(p.getAccount().getName())
                            .email(p.getAccount().getEmail())
                            .phone(p.getAccount().getPhone())
                            .degree(p.getDegree())
                            .yearsExperience(p.getYearsExperience())
                            .majorId(major.getId())
                            .majorName(major.getName())
                            .build())
                    .collect(Collectors.toList());

            return ResponseDto.success(responses,
                    "Found " + responses.size() + " lecturer(s) in " + major.getName());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * Initialize majors khi khởi động hệ thống
     */
    @Transactional
    public void initializeMajors() {
        if (majorRepository.count() > 0) {
            return;
        }

        List<Major> majors = List.of(
                Major.builder().name("Công nghệ thông tin").code("IT").description("Information Technology").isActive(true).build(),
                Major.builder().name("Kỹ thuật phần mềm").code("SE").description("Software Engineering").isActive(true).build(),
                Major.builder().name("An toàn thông tin").code("IA").description("Information Assurance").isActive(true).build(),
                Major.builder().name("Trí tuệ nhân tạo").code("AI").description("Artificial Intelligence").isActive(true).build(),
                Major.builder().name("Khoa học dữ liệu").code("DS").description("Data Science").isActive(true).build(),
                Major.builder().name("Thiết kế đồ họa").code("GD").description("Graphic Design").isActive(true).build(),
                Major.builder().name("Marketing").code("MKT").description("Marketing").isActive(true).build(),
                Major.builder().name("Quản trị kinh doanh").code("BA").description("Business Administration").isActive(true).build(),
                Major.builder().name("Ngôn ngữ Anh").code("EN").description("English Language").isActive(true).build(),
                Major.builder().name("Ngôn ngữ Nhật").code("JP").description("Japanese Language").isActive(true).build()
        );

        majorRepository.saveAll(majors);
        System.out.println("✅ Initialized " + majors.size() + " majors");
    }
}