package com.example.smrsservice.controller;

import com.example.smrsservice.dto.account.*;
import com.example.smrsservice.dto.auth.LoginRequest;
import com.example.smrsservice.dto.auth.LoginResponseDto;
import com.example.smrsservice.dto.common.ResponseDto;
import com.example.smrsservice.entity.Account;
import com.example.smrsservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * ✅ API 1: Login
     */
    @PostMapping("/login")
    public ResponseEntity<ResponseDto<LoginResponseDto>> login(@RequestBody LoginRequest request) {
        ResponseDto<LoginResponseDto> response = accountService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ API 2: Tạo account mới
     */
    @PostMapping
    public ResponseEntity<CreateResponseDto> createAccount(@RequestBody CreateAccountDto request) {
        CreateResponseDto response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * ✅ API 3: Lấy thông tin account hiện tại (từ token)
     * GET /api/accounts/me
     */
    @GetMapping("/me")
    public ResponseEntity<ResponseDto<AccountDto>> getMe(Authentication authentication) {
        ResponseDto<AccountDto> response = accountService.getMe(authentication);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ API 4: Update account hiện tại (KHÔNG CẦN {id})
     * PUT /api/accounts/update
     *
     * Request Body:
     * {
     *   "name": "string",
     *   "phone": "string",
     *   "avatar": "string",
     *   "age": 0
     * }
     */
    @PutMapping("/update")
    public ResponseEntity<ResponseDto<AccountDetailResponse>> updateAccount(
            @RequestBody UpdateAccountDto request,
            Authentication authentication) {

        ResponseDto<AccountDetailResponse> response = accountService.updateAccount(request, authentication);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ API 5: Lấy danh sách tất cả accounts (phân trang)
     */
    @GetMapping
    public ResponseEntity<PageResponse<AccountDetailResponse>> getAccountDetail(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<AccountDetailResponse> response = accountService.getAccountDetail(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ API 6: Khóa account
     */
    @PutMapping("/{id}/lock")
    public ResponseEntity<String> lockAccount(@PathVariable Integer id) throws AccountNotFoundException {
        accountService.lockAccount(id);
        return ResponseEntity.ok("Account locked successfully");
    }

    /**
     * ✅ API 7: Mở khóa account
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<String> activateAccount(@PathVariable Integer id) throws AccountNotFoundException {
        accountService.activateAccount(id);
        return ResponseEntity.ok("Account activated successfully");
    }

    /**
     * ✅ API 8: Xóa account
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable Integer id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok("Account deleted successfully");
    }

    /**
     * ✅ API 9: Import accounts từ Excel
     */
    @PostMapping("/import")
    public ResponseEntity<List<Account>> importAccounts(@RequestParam("file") MultipartFile file) {
        List<Account> accounts = accountService.importAccountsFromExcel(file);
        return ResponseEntity.ok(accounts);
    }

    /**
     * ✅ API 10: Forgot password (gửi mật khẩu tạm qua email)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        accountService.forgotPasswordSimple(request);
        return ResponseEntity.ok("Temporary password has been sent to your email");
    }

    /**
     * ✅ API 11: Change password (đã đăng nhập)
     */
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        accountService.changePassword(request, authentication);
        return ResponseEntity.ok("Password changed successfully");
    }
}