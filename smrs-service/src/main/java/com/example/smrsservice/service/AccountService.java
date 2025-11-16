package com.example.smrsservice.service;

import com.example.smrsservice.common.AccountStatus;
import com.example.smrsservice.config.PasswordEncoderConfig;
import com.example.smrsservice.dto.account.*;
import com.example.smrsservice.dto.auth.LoginRequest;
import com.example.smrsservice.dto.auth.LoginResponseDto;
import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.entity.Account;
import com.example.smrsservice.entity.Role;
import com.example.smrsservice.repository.AccountRepository;
import com.example.smrsservice.repository.RoleRepository;
import com.example.smrsservice.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.login.AccountNotFoundException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenUtil jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PasswordEncoderConfig passwordEncoderConfig;
    private final MailService mailService;

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

    public List<Account> importAccountsFromExcel(MultipartFile file) {
        List<Account> accounts = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
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
                    acc.setPassword(passwordEncoder.encode(
                            getCellValue(row.getCell(headerMap.get("password")))));
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
                }

                if (headerMap.containsKey("role")) {
                    String roleName = getCellValue(row.getCell(headerMap.get("role")));
                    Role role = roleRepository.findByRoleName(roleName).orElseThrow();
                    acc.setRole(role);
                }

                accounts.add(acc);
            }

            accountRepository.saveAll(accounts);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file Excel: " + e.getMessage(), e);
        }

        return accounts;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC)
            return String.valueOf((int) cell.getNumericCellValue());
        return "";
    }

    public PageResponse<AccountDetailResponse> getAccountDetail(int page , int size) {

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Account> accounts = accountRepository.findAll(pageable);

        List<Account> accountList = accounts.getContent();

        return PageResponse.<AccountDetailResponse>builder()
                .currentPages(page)
                .pageSizes(pageable.getPageSize())
                .totalPages(accounts.getTotalPages())
                .totalElements(accounts.getTotalElements())
                .data(accountList.stream().map(account -> AccountDetailResponse.builder()
                        .id(account.getId())
                        .email(account.getEmail())
                        .avatar(account.getAvatar())
                        .phone(account.getPhone())
                        .name(account.getName())
                        .age(account.getAge())
                        .status(account.getStatus() != null ? account.getStatus().name() : null)
                        .role(account.getRole())
                        .locked(account.getStatus() != null && account.getStatus() == AccountStatus.LOCKED)
                        .build()).toList())
                .build();

    }
    public void deleteAccount(Integer id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khoong tìm thấy tài khoản"));

        accountRepository.deleteById(account.getId());
    }

    /**
     * ✅ UPDATED: Lấy id từ token thay vì path parameter
     * Sử dụng Authentication để lấy thông tin user hiện tại
     */
    @Transactional
    public  ResponseDto<AccountDetailResponse> updateAccount(UpdateAccountDto request, Authentication authentication) {
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
                    .role(account.getRole())
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
            AccountDto dto = AccountDto.builder()
                    .id(Long.valueOf(account.getId()))
                    .name(account.getName())
                    .email(account.getEmail())
                    .phone(account.getPhone())
                    .role(account.getRole() != null ? account.getRole().getRoleName() : null)
                    .age(account.getAge())
                    .build();
            return ResponseDto.success(dto, "OK");
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

    /** Sinh mật khẩu tạm dạng Base64URL (không ký tự lạ, dễ gõ). */
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

}