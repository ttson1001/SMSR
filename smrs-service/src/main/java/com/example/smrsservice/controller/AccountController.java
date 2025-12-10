package com.example.smrsservice.controller;

import com.example.smrsservice.common.AccountStatus;
import com.example.smrsservice.dto.account.*;
import com.example.smrsservice.dto.auth.LoginRequest;
import com.example.smrsservice.dto.auth.LoginResponseDto;
import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.entity.Account;
import com.example.smrsservice.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
@PreAuthorize("isAuthenticated()")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Login and get JWT token")
    public ResponseDto<LoginResponseDto> login(@RequestBody LoginRequest request) {
        return accountService.login(request);
    }

    @PatchMapping("/{id}/lock")
    public ResponseEntity<Void> lock(@PathVariable Integer id) throws AccountNotFoundException {
        accountService.lockAccount(id);
        return ResponseEntity.noContent().build(); // 204
    }

    @PatchMapping("/{id}/activate")

    public ResponseEntity<Void> activate(@PathVariable Integer id) throws AccountNotFoundException {
        accountService.activateAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create")
    void createAccount(@RequestBody CreateAccountDto request) {
        accountService.createAccount(request);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importAccounts(
            @Parameter(description = "File Excel chứa danh sách account", required = true)
            @RequestPart("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File không được để trống!");
        }

        try {
            List<Account> importedAccounts = accountService.importAccountsFromExcel(file);
            return ResponseEntity.ok(importedAccounts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi import file: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<PageResponse<AccountDetailResponse>> getAllAccounts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {

        // Convert string status to enum
        AccountStatus accountStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                accountStatus = AccountStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }

        PageResponse<AccountDetailResponse> response = accountService.getAccountDetail(
                page, size, name, email, role, accountStatus
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    void deleteAccountById(@PathVariable Integer id) {
        accountService.deleteAccount(id);
    }

    /**
     * ✅ FIXED: Update account không cần {id}, lấy từ token
     */
    @PutMapping("/update")
    public ResponseDto<AccountDetailResponse> updateAccount(
            @RequestBody UpdateAccountDto request,
            Authentication authentication) {
        return accountService.updateAccount(request, authentication);
    }

    @GetMapping("/me")
    public ResponseDto<AccountDto> me(Authentication authentication){
        return accountService.getMe(authentication);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        accountService.forgotPasswordSimple(req);
        // Luôn trả OK để không lộ email tồn tại hay không
        return ResponseEntity.ok(Map.of("message", "Nếu email tồn tại, mật khẩu tạm đã được gửi."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req,
                                            Authentication auth) {
        accountService.changePassword(req, auth);
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }

    @PostMapping("/import-deans")
    public ResponseEntity<ResponseDto<ImportDeanResult>> importDeans(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        // Check role ADMIN
        Account currentUser = getCurrentAccount(authentication);
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ResponseDto.fail("Only admins can import deans"));
        }

        ResponseDto<ImportDeanResult> response = accountService.importDeansFromExcel(file);

        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        ).body(response);
    }


    private Account getCurrentAccount(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Account) {
            return (Account) principal;
        }

        if (principal instanceof String) {
            String email = (String) principal;
            return accountService.getAccountByEmail(email);  // ✅ GỌI SERVICE
        }

        throw new RuntimeException("Invalid authentication principal");
    }
}