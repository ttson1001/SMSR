package com.example.smrsservice.service;

import com.example.smrsservice.common.AccountStatus;
import com.example.smrsservice.common.CouncilManagerStatus;
import com.example.smrsservice.config.PasswordEncoderConfig;
import com.example.smrsservice.dto.account.*;
import com.example.smrsservice.dto.auth.LoginRequest;
import com.example.smrsservice.dto.auth.LoginResponseDto;
import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.entity.*;
import com.example.smrsservice.repository.*;
import com.example.smrsservice.security.JwtTokenUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.login.AccountNotFoundException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenUtil jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final MailService mailService;
    private final CouncilManagerProfileRepository councilProfileRepository;
    private final MajorRepository majorRepository;
    private final LecturerProfileRepository lecturerProfileRepository;


    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found with email: " + email));
    }

    public ResponseDto<LoginResponseDto> login(LoginRequest request) {
        var acc = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found"));


        if (!passwordEncoder.matches(request.getPassword(), acc.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(acc.getEmail(), acc.getRole().getRoleName());

        LoginResponseDto response = new LoginResponseDto();
        response.setToken(token);
        response.setEmail(acc.getEmail());
        response.setRole(acc.getRole().getRoleName());

        return ResponseDto.success(response, "Đăng nhập thành công");
    }

    public void lockAccount(Integer id) throws AccountNotFoundException {
        var account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account không tồn tại"));
        if (account.getStatus() == AccountStatus.LOCKED) {
            throw new AccountNotFoundException("Tài Khoản Đã Bị Khóa");
        }
        account.setStatus(AccountStatus.LOCKED);
        accountRepository.save(account);

    }

    public void activateAccount(Integer id) throws AccountNotFoundException {
        var account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account không tồn tại"));
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    public CreateResponseDto createAccount(CreateAccountDto request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");

        }
        Account account = Account.builder()
                .email(request.getEmail())
                .name(request.getName())
                .age(request.getAge())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRoleId())
                .status(AccountStatus.ACTIVE)
                .build();

        accountRepository.save(account);

        return CreateResponseDto.builder()
                .accountId(account.getId())
                .email(account.getEmail())
                .name(account.getName())
                .role(account.getRole())
                .status(account.getStatus())
                .build();

    }

    /**
     * ⭐ IMPORT ACCOUNTS - XỬ LÝ CẢ 3 ROLES: STUDENT, LECTURER, DEAN
     */
    public List<Account> importAccountsFromExcel(MultipartFile file) {
        List<Account> accounts = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Đọc header row
            Map<String, Integer> headerMap = new HashMap<>();
            if (rows.hasNext()) {
                Row headerRow = rows.next();
                for (Cell cell : headerRow) {
                    String columnName = cell.getStringCellValue().trim().toLowerCase();
                    headerMap.put(columnName, cell.getColumnIndex());
                }
            }

            // Kiểm tra các cột bắt buộc
            if (!headerMap.containsKey("email")) {
                throw new RuntimeException("Missing required column: email");
            }

            // Đọc data rows
            while (rows.hasNext()) {
                Row row = rows.next();

                String email = getCellValue(row.getCell(headerMap.get("email")));
                if (email == null || email.isBlank()) continue;

                Account acc = accountRepository.findByEmail(email).orElse(new Account());
                boolean isNew = (acc.getId() == null);

                if (isNew) {
                    acc.setEmail(email);
                    acc.setCreateDate(new Date());
                }

                // Đọc theo tên cột trong header
                if (headerMap.containsKey("password")) {
                    String password = getCellValue(row.getCell(headerMap.get("password")));
                    if (password != null && !password.isBlank()) {
                        acc.setPassword(passwordEncoder.encode(password));
                    } else if (isNew) {
                        // Generate temp password for new accounts
                        String tempPassword = generateTempPassword(12);
                        acc.setPassword(passwordEncoder.encode(tempPassword));
                        System.out.println("Generated temp password for " + email + ": " + tempPassword);
                    }
                } else if (isNew) {
                    // Generate temp password if no password column
                    String tempPassword = generateTempPassword(12);
                    acc.setPassword(passwordEncoder.encode(tempPassword));
                    System.out.println("Generated temp password for " + email + ": " + tempPassword);
                }

                if (headerMap.containsKey("name")) {
                    acc.setName(getCellValue(row.getCell(headerMap.get("name"))));
                }

                if (headerMap.containsKey("avatar")) {
                    acc.setAvatar(getCellValue(row.getCell(headerMap.get("avatar"))));
                }

                if (headerMap.containsKey("phone")) {
                    acc.setPhone(getCellValue(row.getCell(headerMap.get("phone"))));
                }

                if (headerMap.containsKey("age")) {
                    String ageStr = getCellValue(row.getCell(headerMap.get("age")));
                    if (!ageStr.isEmpty()) {
                        acc.setAge(Integer.parseInt(ageStr));
                    }
                }

                if (headerMap.containsKey("status")) {
                    String statusValue = getCellValue(row.getCell(headerMap.get("status"))).toUpperCase();
                    acc.setStatus(statusValue.equals("LOCKED") ? AccountStatus.LOCKED : AccountStatus.ACTIVE);
                } else {
                    acc.setStatus(AccountStatus.ACTIVE);
                }

                // ========== ⭐ ĐỌC ROLE ==========
                String roleName = null;
                if (headerMap.containsKey("role")) {
                    roleName = getCellValue(row.getCell(headerMap.get("role")));
                    Role role = roleRepository.findByRoleName(roleName).orElseThrow();
                    acc.setRole(role);
                }

                accounts.add(acc);
                accountRepository.save(acc); // ⭐ SAVE TRƯỚC để có ID

                // ========== ⭐ XỬ LÝ THEO ROLE ==========
                if (roleName != null) {
                    try {
                        if ("LECTURER".equalsIgnoreCase(roleName)) {
                            handleLecturerProfile(acc, row, headerMap);
                        } else if ("DEAN".equalsIgnoreCase(roleName)) {
                            handleDeanProfile(acc, row, headerMap);
                        }
                        // STUDENT không cần profile, chỉ có Account
                    } catch (Exception e) {
                        System.err.println("⚠️ Failed to create profile for " + email + ": " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file Excel: " + e.getMessage(), e);
        }

        return accounts;
    }

    /**
     * Xử lý tạo/update LecturerProfile
     */
    private void handleLecturerProfile(Account account, Row row, Map<String, Integer> headerMap) {
        LecturerProfile profile = lecturerProfileRepository
                .findByAccountId(account.getId())
                .orElse(new LecturerProfile());

        if (profile.getId() == null) {
            profile.setAccount(account);
        }

        // ⭐ MAJOR (required cho LECTURER)
        if (headerMap.containsKey("majorid")) {
            String majorIdStr = getCellValue(row.getCell(headerMap.get("majorid")));
            if (majorIdStr != null && !majorIdStr.isBlank()) {
                Integer majorId = Integer.parseInt(majorIdStr);
                Major major = majorRepository.findById(majorId)
                        .orElseThrow(() -> new RuntimeException("Major not found: " + majorId));
                profile.setMajor(major);
                System.out.println("✅ Assigned major " + major.getName() + " to lecturer " + account.getEmail());
            }
        }

        // Teaching Major
        if (headerMap.containsKey("teachingmajor")) {
            profile.setTeachingMajor(getCellValue(row.getCell(headerMap.get("teachingmajor"))));
        }

        // Degree
        if (headerMap.containsKey("degree")) {
            profile.setDegree(getCellValue(row.getCell(headerMap.get("degree"))));
        }

        // Years Experience
        if (headerMap.containsKey("yearsexperience")) {
            String yearsStr = getCellValue(row.getCell(headerMap.get("yearsexperience")));
            if (!yearsStr.isEmpty()) {
                profile.setYearsExperience(Integer.parseInt(yearsStr));
            }
        }
        lecturerProfileRepository.save(profile);
    }

    /**
     * Xử lý tạo/update CouncilManagerProfile (DEAN)
     */
    private void handleDeanProfile(Account account, Row row, Map<String, Integer> headerMap) {
        CouncilManagerProfile profile = councilProfileRepository
                .findByAccountId(account.getId())
                .orElse(new CouncilManagerProfile());

        if (profile.getId() == null) {
            profile.setAccount(account);
            profile.setStatus(CouncilManagerStatus.ACTIVE);
            profile.setStartDate(LocalDate.now());
        }

        // ⭐ MAJOR (required cho DEAN)
        if (headerMap.containsKey("majorid")) {
            String majorIdStr = getCellValue(row.getCell(headerMap.get("majorid")));
            if (majorIdStr != null && !majorIdStr.isBlank()) {
                Integer majorId = Integer.parseInt(majorIdStr);
                Major major = majorRepository.findById(majorId)
                        .orElseThrow(() -> new RuntimeException("Major not found: " + majorId));
                profile.setMajor(major);
                System.out.println("✅ Assigned major " + major.getName() + " to dean " + account.getEmail());
            }
        }

        // Employee Code
        if (headerMap.containsKey("employeecode")) {
            profile.setEmployeeCode(getCellValue(row.getCell(headerMap.get("employeecode"))));
        }

        // Position Title
        if (headerMap.containsKey("positiontitle")) {
            profile.setPositionTitle(getCellValue(row.getCell(headerMap.get("positiontitle"))));
        }

        // Department
        if (headerMap.containsKey("department")) {
            profile.setDepartment(getCellValue(row.getCell(headerMap.get("department"))));
        }

        councilProfileRepository.save(profile);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC)
            return String.valueOf((int) cell.getNumericCellValue());
        return "";
    }

    public PageResponse<AccountDetailResponse> getAccountDetail(
            int page,
            int size,
            String name,
            String email,
            String role,
            AccountStatus status) {

        Pageable pageable = PageRequest.of(page - 1, size);

        // Build Specification để filter
        Specification<Account> spec = buildAccountSpecification(name, email, role, status);

        Page<Account> accounts = accountRepository.findAll(spec, pageable);

        List<Account> accountList = accounts.getContent();

        // ========== MAP TO RESPONSE VỚI MAJOR INFO CHO DEAN ==========
        List<AccountDetailResponse> responseList = accountList.stream()
                .map(account -> {
                    // Build basic account info
                    AccountDetailResponse.AccountDetailResponseBuilder builder = AccountDetailResponse.builder()
                            .id(account.getId())
                            .email(account.getEmail())
                            .avatar(account.getAvatar())
                            .phone(account.getPhone())
                            .name(account.getName())
                            .age(account.getAge())
                            .status(account.getStatus() != null ? account.getStatus().name() : null)
                            .locked(account.getStatus() != null && account.getStatus() == AccountStatus.LOCKED);

                    // Role info - ⭐ SỬA: Dùng nested RoleInfo
                    if (account.getRole() != null) {
                        builder.role(AccountDetailResponse.RoleInfo.builder()
                                .id(account.getRole().getId())
                                .roleName(account.getRole().getRoleName())
                                .build());
                    }

                    // ========== ⭐ THÊM MAJOR INFO CHO DEAN ==========
                    if (account.getRole() != null && "DEAN".equalsIgnoreCase(account.getRole().getRoleName())) {
                        Optional<CouncilManagerProfile> profileOpt = councilProfileRepository
                                .findByAccountId(account.getId());

                        if (profileOpt.isPresent()) {
                            CouncilManagerProfile profile = profileOpt.get();

                            // Set major info
                            if (profile.getMajor() != null) {
                                builder.major(AccountDetailResponse.MajorInfo.builder()
                                        .id(profile.getMajor().getId())
                                        .name(profile.getMajor().getName())
                                        .code(profile.getMajor().getCode())
                                        .description(profile.getMajor().getDescription())
                                        .build());
                            }

                            // Set council manager profile info
                            builder.councilManagerProfile(AccountDetailResponse.CouncilManagerInfo.builder()
                                    .profileId(profile.getId())
                                    .employeeCode(profile.getEmployeeCode())
                                    .positionTitle(profile.getPositionTitle())
                                    .department(profile.getDepartment())
                                    .status(profile.getStatus() != null ? profile.getStatus().name() : null)
                                    .build());
                        }
                    }

                    return builder.build();
                })
                .toList();

        return PageResponse.<AccountDetailResponse>builder()
                .currentPages(page)
                .pageSizes(pageable.getPageSize())
                .totalPages(accounts.getTotalPages())
                .totalElements(accounts.getTotalElements())
                .data(responseList)
                .build();
    }

    /**
     * ✅ UPDATED: Lấy id từ token thay vì path parameter
     * Sử dụng Authentication để lấy thông tin user hiện tại
     */
    @Transactional
    public ResponseDto<AccountDetailResponse> updateAccount(UpdateAccountDto request, Authentication authentication) {
        try {
            // Lấy account từ token
            Account account = currentAccount(authentication);

            // Update các field nếu có trong request
            if (request.getName() != null && !request.getName().isBlank()) {
                account.setName(request.getName());
            }

            if (request.getPhone() != null && !request.getPhone().isBlank()) {
                account.setPhone(request.getPhone());
            }

            if (request.getAvatar() != null && !request.getAvatar().isBlank()) {
                account.setAvatar(request.getAvatar());
            }

            if (request.getAge() != null) {
                account.setAge(request.getAge());
            }

            accountRepository.save(account);

            AccountDetailResponse response = AccountDetailResponse.builder()
                    .id(account.getId())
                    .email(account.getEmail())
                    .name(account.getName())
                    .phone(account.getPhone())
                    .avatar(account.getAvatar())
                    .age(account.getAge())
                    .status(account.getStatus() != null ? account.getStatus().name() : null)
                    .role(AccountDetailResponse.RoleInfo.builder()
                            .id(account.getRole().getId())
                            .roleName(account.getRole().getRoleName())
                            .build())
                    .locked(account.getStatus() != null && account.getStatus() == AccountStatus.LOCKED)
                    .build();

            return ResponseDto.success(response, "Account updated successfully");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    public ResponseDto<AccountDto> getMe(Authentication authentication) {
        try {
            Account account = currentAccount(authentication);

            // Build basic account info
            AccountDto.AccountDtoBuilder builder = AccountDto.builder()
                    .id(account.getId())
                    .name(account.getName())
                    .email(account.getEmail())
                    .phone(account.getPhone())
                    .avatar(account.getAvatar())
                    .age(account.getAge())
                    .role(account.getRole() != null ? account.getRole().getRoleName() : null);

            // ========== ⭐ THÊM MAJOR INFO CHO DEAN ==========
            if (account.getRole() != null && "DEAN".equalsIgnoreCase(account.getRole().getRoleName())) {
                Optional<CouncilManagerProfile> profileOpt = councilProfileRepository
                        .findByAccountId(account.getId());

                if (profileOpt.isPresent()) {
                    CouncilManagerProfile profile = profileOpt.get();

                    // Set major info
                    if (profile.getMajor() != null) {
                        builder.major(AccountDto.MajorInfo.builder()
                                .id(profile.getMajor().getId())
                                .name(profile.getMajor().getName())
                                .code(profile.getMajor().getCode())
                                .description(profile.getMajor().getDescription())
                                .build());
                    }

                    // Set council manager profile info
                    builder.councilManagerProfile(AccountDto.CouncilManagerInfo.builder()
                            .profileId(profile.getId())
                            .employeeCode(profile.getEmployeeCode())
                            .positionTitle(profile.getPositionTitle())
                            .department(profile.getDepartment())
                            .status(profile.getStatus() != null ? profile.getStatus().name() : null)
                            .build());
                }
            }

            return ResponseDto.success(builder.build(), "OK");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    private String extractEmail(Authentication authentication) {
        if (authentication == null)
            throw new IllegalStateException("User chưa đăng nhập");

        Object principal = authentication.getPrincipal();

        // ✅ Nếu principal là Account (custom entity)
        if (principal instanceof Account acc) {
            if (acc.getEmail() == null || acc.getEmail().isBlank()) {
                throw new IllegalStateException("Account không có email");
            }
            return acc.getEmail();
        }

        // ✅ Nếu principal là UserDetails mặc định của Spring
        if (principal instanceof org.springframework.security.core.userdetails.User user) {
            return user.getUsername(); // username thường là email
        }

        // ✅ Nếu principal là String (JWT filter custom)
        if (principal instanceof String s && !s.isBlank()) {
            return s;
        }

        throw new IllegalStateException("Không lấy được email từ Authentication");
    }

    private Account currentAccount(Authentication authentication) {
        String email = extractEmail(authentication);
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
    }

    // 2.1 Quên mật khẩu — tạo mật khẩu tạm và email cho user
    public void forgotPasswordSimple(ForgotPasswordRequest req) {
        if (req == null || req.getEmail() == null || req.getEmail().isBlank()) return;

        // Không lộ thông tin tài khoản: nếu ko có email thì cũng trả OK
        Optional<Account> accOpt = accountRepository.findByEmail(req.getEmail().trim());
        if (accOpt.isEmpty()) return;

        Account acc = accOpt.get();

        String tempPassword = generateTempPassword(12); // độ dài tuỳ chỉnh
        acc.setPassword(passwordEncoder.encode(tempPassword));
        accountRepository.save(acc);

        String subject = "[SMRS] Mật khẩu tạm cho tài khoản của bạn";
        String body = """
                Xin chào %s,

                Hệ thống vừa tạo MẬT KHẨU TẠM cho tài khoản của bạn:
                %s

                Vui lòng đăng nhập bằng mật khẩu tạm này và ĐỔI MẬT KHẨU NGAY trong mục Tài khoản.
                Nếu không phải bạn yêu cầu, hãy bỏ qua email này.

                Trân trọng.
                """.formatted(Optional.ofNullable(acc.getName()).orElse("bạn"), tempPassword);

        try {
            mailService.sendSimpleMail(acc.getEmail(), subject, body);
        } catch (Exception e) {
            // Cho môi trường dev/chưa cấu hình SMTP: in ra console để test
            System.out.println("[DEV] Temp password for " + acc.getEmail() + ": " + tempPassword);
        }
    }

    // 2.2 Đổi mật khẩu khi đã đăng nhập
    public void changePassword(ChangePasswordRequest req, Authentication authentication) {
        if (req == null || req.getNewPassword() == null) {
            throw new IllegalArgumentException("Thiếu thông tin mật khẩu");
        }
        if (req.getConfirmPassword() != null && !req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("Password confirmation không khớp");
        }
        validatePasswordStrength(req.getNewPassword());

        Account acc = currentAccount(authentication);  // <-- Lấy đúng account từ principal/DB

        // Kiểm tra oldPassword dựa trên mật khẩu hiện tại (bao gồm mật khẩu tạm sau forgot)
        if (req.getOldPassword() == null || !passwordEncoder.matches(req.getOldPassword(), acc.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }

        acc.setPassword(passwordEncoder.encode(req.getNewPassword()));
        accountRepository.save(acc);
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Sinh mật khẩu tạm dạng Base64URL (không ký tự lạ, dễ gõ).
     */
    private static String generateTempPassword(int numBytes) {
        byte[] buf = new byte[numBytes];
        SECURE_RANDOM.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    private static void validatePasswordStrength(String pwd) {
        if (pwd.length() < 8) throw new IllegalArgumentException("Password tối thiểu 8 ký tự");
    }


    /**
     * ⚠️ DEPRECATED: Sử dụng updateAccount() thay thế
     * Method này bị duplicate với updateAccount()
     */
    @Deprecated
    @Transactional
    public ResponseDto<Account> updateProfile(UpdateAccountDto request) {
        try {
            // Lấy user hiện tại từ SecurityContext
            Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();

            Account currentUser = currentAccount(authentication);

            if (request.getName() != null) {
                currentUser.setName(request.getName());
            }

            if (request.getPhone() != null) {
                currentUser.setPhone(request.getPhone());
            }

            if (request.getAvatar() != null) {
                currentUser.setAvatar(request.getAvatar());
            }

            if (request.getAge() != null) {
                currentUser.setAge(request.getAge());
            }

            accountRepository.save(currentUser);

            return ResponseDto.success(currentUser, "Profile updated successfully");

        } catch (Exception e) {
            return ResponseDto.fail(e.getMessage());
        }
    }

    /**
     * Build Specification để filter accounts
     */
    private Specification<Account> buildAccountSpecification(
            String name,
            String email,
            String role,
            AccountStatus status) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by name (partial match, case-insensitive)
            if (name != null && !name.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                "%" + name.toLowerCase().trim() + "%"
                        )
                );
            }

            // Filter by email (partial match, case-insensitive)
            if (email != null && !email.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("email")),
                                "%" + email.toLowerCase().trim() + "%"
                        )
                );
            }

            // Filter by role (exact match)
            if (role != null && !role.isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("role").get("roleName"),
                                role.trim()
                        )
                );
            }

            // Filter by status (exact match)
            if (status != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("status"), status)
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * ⭐ IMPORT DEANS - Dedicated method for Dean import with full details
     */
    public ResponseDto<ImportDeanResult> importDeansFromExcel(MultipartFile file) {
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // ========== ĐỌC HEADER ==========
            Map<String, Integer> headerMap = new HashMap<>();
            if (rows.hasNext()) {
                Row headerRow = rows.next();
                for (Cell cell : headerRow) {
                    String columnName = cell.getStringCellValue().trim().toLowerCase();
                    headerMap.put(columnName, cell.getColumnIndex());
                }
            }

            // ========== VALIDATE REQUIRED COLUMNS ==========
            List<String> requiredColumns = List.of("email", "name", "majorid");
            for (String col : requiredColumns) {
                if (!headerMap.containsKey(col)) {
                    return ResponseDto.fail("Missing required column: " + col);
                }
            }

            // ========== ĐỌC DATA ==========
            List<String> successEmails = new ArrayList<>();
            List<String> failedEmails = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            // ⭐ THÊM: List chứa dean details
            List<ImportDeanResult.DeanDetail> successDeans = new ArrayList<>();

            Role deanRole = roleRepository.findByRoleName("DEAN")
                    .orElseThrow(() -> new RuntimeException("DEAN role not found"));

            while (rows.hasNext()) {
                Row row = rows.next();
                String email = getCellValue(row.getCell(headerMap.get("email")));

                if (email == null || email.isBlank()) continue;

                try {
                    // ⭐ THÊM: Biến track password generation
                    String generatedPassword = null;
                    boolean passwordGenerated = false;

                    // ========== 1. TẠO/UPDATE ACCOUNT ==========
                    Account account = accountRepository.findByEmail(email)
                            .orElse(new Account());

                    boolean isNew = (account.getId() == null);

                    if (isNew) {
                        account.setEmail(email);
                        account.setCreateDate(new Date());
                        account.setRole(deanRole);
                        account.setStatus(AccountStatus.ACTIVE);
                    }

                    // Name (required)
                    String name = getCellValue(row.getCell(headerMap.get("name")));
                    if (name == null || name.isBlank()) {
                        failedEmails.add(email);
                        errors.add(email + ": Missing name");
                        continue;
                    }
                    account.setName(name);

                    // Password (optional, generate if not provided)
                    if (headerMap.containsKey("password")) {
                        String password = getCellValue(row.getCell(headerMap.get("password")));
                        if (password != null && !password.isBlank()) {
                            account.setPassword(passwordEncoder.encode(password));
                        } else if (isNew) {
                            // Generate temp password for new accounts
                            generatedPassword = generateTempPassword(12);
                            account.setPassword(passwordEncoder.encode(generatedPassword));
                            passwordGenerated = true;
                            System.out.println("Generated temp password for " + email + ": " + generatedPassword);
                        }
                    } else if (isNew) {
                        generatedPassword = generateTempPassword(12);
                        account.setPassword(passwordEncoder.encode(generatedPassword));
                        passwordGenerated = true;
                        System.out.println("Generated temp password for " + email + ": " + generatedPassword);
                    }

                    // Phone (optional)
                    if (headerMap.containsKey("phone")) {
                        account.setPhone(getCellValue(row.getCell(headerMap.get("phone"))));
                    }

                    // Age (optional)
                    if (headerMap.containsKey("age")) {
                        String ageStr = getCellValue(row.getCell(headerMap.get("age")));
                        if (!ageStr.isEmpty()) {
                            account.setAge(Integer.parseInt(ageStr));
                        }
                    }

                    accountRepository.save(account);

                    // ========== 2. TẠO/UPDATE COUNCIL MANAGER PROFILE ==========
                    CouncilManagerProfile profile = councilProfileRepository
                            .findByAccountId(account.getId())
                            .orElse(new CouncilManagerProfile());

                    if (profile.getId() == null) {
                        profile.setAccount(account);
                        profile.setStatus(CouncilManagerStatus.ACTIVE);
                        profile.setStartDate(LocalDate.now());
                    }

                    // Major ID (required)
                    String majorIdStr = getCellValue(row.getCell(headerMap.get("majorid")));
                    if (majorIdStr == null || majorIdStr.isBlank()) {
                        failedEmails.add(email);
                        errors.add(email + ": Missing majorId");
                        continue;
                    }

                    Integer majorId = Integer.parseInt(majorIdStr);
                    Major major = majorRepository.findById(majorId)
                            .orElseThrow(() -> new RuntimeException("Major not found: " + majorId));

                    profile.setMajor(major);

                    // Department (optional, default to major name)
                    if (headerMap.containsKey("department")) {
                        String dept = getCellValue(row.getCell(headerMap.get("department")));
                        profile.setDepartment(dept != null && !dept.isBlank() ? dept : major.getName());
                    } else {
                        profile.setDepartment(major.getName());
                    }

                    // Employee Code (optional)
                    if (headerMap.containsKey("employeecode")) {
                        profile.setEmployeeCode(getCellValue(row.getCell(headerMap.get("employeecode"))));
                    }

                    // Position Title (optional)
                    if (headerMap.containsKey("positiontitle")) {
                        String position = getCellValue(row.getCell(headerMap.get("positiontitle")));
                        profile.setPositionTitle(position != null && !position.isBlank()
                                ? position
                                : "Trưởng bộ môn " + major.getName());
                    } else {
                        profile.setPositionTitle("Trưởng bộ môn " + major.getName());
                    }

                    councilProfileRepository.save(profile);

                    // ========== ⭐ BUILD DEAN DETAIL ==========
                    ImportDeanResult.DeanDetail deanDetail = ImportDeanResult.DeanDetail.builder()
                            // Account info
                            .accountId(account.getId())
                            .email(account.getEmail())
                            .name(account.getName())
                            .phone(account.getPhone())
                            .age(account.getAge())
                            .status(account.getStatus().toString())
                            .role(account.getRole().getRoleName())

                            // Profile info
                            .profileId(profile.getId())
                            .employeeCode(profile.getEmployeeCode())
                            .positionTitle(profile.getPositionTitle())
                            .department(profile.getDepartment())

                            // Major info
                            .majorId(major.getId())
                            .majorName(major.getName())
                            .majorCode(major.getCode())

                            // Auto-generated info
                            .isNewAccount(isNew)
                            .passwordGenerated(passwordGenerated)
                            .generatedPassword(passwordGenerated ? generatedPassword : null)

                            .build();

                    successDeans.add(deanDetail);
                    successEmails.add(email);
                    System.out.println("✅ Imported dean: " + email + " (Major: " + major.getName() + ")");

                } catch (Exception e) {
                    failedEmails.add(email);
                    errors.add(email + ": " + e.getMessage());
                    System.err.println("❌ Failed to import " + email + ": " + e.getMessage());
                }
            }

            // ========== BUILD RESPONSE ==========
            ImportDeanResult result = ImportDeanResult.builder()
                    .totalRows(successEmails.size() + failedEmails.size())
                    .successCount(successEmails.size())
                    .failedCount(failedEmails.size())
                    .successEmails(successEmails)
                    .failedEmails(failedEmails)
                    .errors(errors)
                    .successDeans(successDeans)  // ⭐ THÊM
                    .build();

            String message = String.format(
                    "Import completed: %d success, %d failed",
                    successEmails.size(), failedEmails.size()
            );

            return ResponseDto.success(result, message);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail("Error reading Excel file: " + e.getMessage());
        }
    }

}