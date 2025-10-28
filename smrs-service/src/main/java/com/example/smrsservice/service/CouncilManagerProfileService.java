package com.example.smrsservice.service;

import com.example.smrsservice.common.CouncilManagerStatus;
import com.example.smrsservice.dto.councilmanagerprofile.CouncilManagerProfileResponse;
import com.example.smrsservice.dto.councilmanagerprofile.CouncilManagerProfileUpdateDto;
import com.example.smrsservice.entity.Account;
import com.example.smrsservice.entity.CouncilManagerProfile;
import com.example.smrsservice.repository.AccountRepository;
import com.example.smrsservice.repository.CouncilManagerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class CouncilManagerProfileService {
    private final CouncilManagerProfileRepository councilManagerProfileRepository;
    private final AccountRepository accountRepository;


    private boolean canEdit(Authentication auth, CouncilManagerProfile p) {
        if (auth == null || auth.getName() == null) return false;
        String email = auth.getName();
        boolean isOwner = p.getAccount() != null && email.equalsIgnoreCase(p.getAccount().getEmail());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));
        return isOwner || isAdmin;
    }

    // PATCH by id
    public CouncilManagerProfileResponse updateById(Integer id, CouncilManagerProfileUpdateDto dto, Authentication auth) {
        CouncilManagerProfile p = councilManagerProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profile không tồn tại"));

        if (!canEdit(auth, p)) throw new SecurityException("Không có quyền cập nhật hồ sơ này");

        applyPatchAndValidate(p, dto);

        councilManagerProfileRepository.save(p);
        return toResponse(p);
    }

    // PATCH cho chính mình (dễ dùng cho FE)
    public CouncilManagerProfileResponse updateMyProfile(CouncilManagerProfileUpdateDto dto, Authentication auth) {
        if (auth == null || auth.getName() == null) throw new SecurityException("Chưa đăng nhập");
        Account me = accountRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Account not found"));
        CouncilManagerProfile p = councilManagerProfileRepository.findByAccountId(me.getId())
                .orElseThrow(() -> new IllegalArgumentException("Bạn chưa có CouncilManagerProfile"));

        // owner đã pass qua canEdit (là chính mình)
        applyPatchAndValidate(p, dto);
        councilManagerProfileRepository.save(p);
        return toResponse(p);
    }

    // —— patch + validate —— //
    private void applyPatchAndValidate(CouncilManagerProfile p, CouncilManagerProfileUpdateDto dto) {
        // employeeCode (unique)
        if (StringUtils.hasText(dto.getEmployeeCode())) {
            String newCode = dto.getEmployeeCode().trim();
            if (!newCode.equals(p.getEmployeeCode())
                    && councilManagerProfileRepository.existsByEmployeeCodeAndIdNot(newCode, p.getId())) {
                throw new IllegalArgumentException("Mã cán bộ đã tồn tại: " + newCode);
            }
            p.setEmployeeCode(newCode);
        }

        if (dto.getDepartment() != null) {
            p.setDepartment(dto.getDepartment().trim());
        }
        if (dto.getPositionTitle() != null) {
            p.setPositionTitle(dto.getPositionTitle().trim());
        }
        if (dto.getStatus() != null) {
            p.setStatus(dto.getStatus());
        }
        if (dto.getStartDate() != null) {
            p.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            p.setEndDate(dto.getEndDate());
        }
        if (dto.getNote() != null) {
            p.setNote(dto.getNote());
        }

        // Validate ngày
        LocalDate s = p.getStartDate();
        LocalDate e = p.getEndDate();
        if (s != null && e != null && e.isBefore(s)) {
            throw new IllegalArgumentException("endDate không thể trước startDate");
        }

        // (Tuỳ chọn) Quy tắc theo status
        if (p.getStatus() == CouncilManagerStatus.INACTIVE || p.getStatus() == CouncilManagerStatus.SUSPENDED) {
            // ví dụ: có thể clear endDate nếu cần, hoặc chặn gán vào Council mới ở nơi khác
        }
    }

    private CouncilManagerProfileResponse toResponse(CouncilManagerProfile p) {
        return CouncilManagerProfileResponse.builder()
                .id(p.getId())
                .accountId(p.getAccount()!=null ? p.getAccount().getId() : null)
                .accountEmail(p.getAccount()!=null ? p.getAccount().getEmail() : null)
                .employeeCode(p.getEmployeeCode())
                .department(p.getDepartment())
                .positionTitle(p.getPositionTitle())
                .status(p.getStatus())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .note(p.getNote())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
