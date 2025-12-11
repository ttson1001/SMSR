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
    @Operation(summary = "Lock account")
    public ResponseEntity<Void> lock(@PathVariable Integer id) throws AccountNotFoundException {
        accountService.lockAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate account")
    public ResponseEntity<Void> activate(@PathVariable Integer id) throws AccountNotFoundException {
        accountService.activateAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create")
    @Operation(summary = "Create new account")
    public ResponseEntity<CreateResponseDto> createAccount(@RequestBody CreateAccountDto request) {
        CreateResponseDto response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * ⭐ IMPORT ACCOUNTS - XỬ LÝ TẤT CẢ ROLES: STUDENT, LECTURER, DEAN
     *
     * Excel columns:
     * - Required: email, role, name
     * - Optional for all: password, phone, age, status
     * - Required for LECTURER: majorId
     * - Optional for LECTURER: teachingMajor, degree, yearsExperience
     * - Required for DEAN: majorId
     * - Optional for DEAN: employeeCode, positionTitle, department
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Import accounts from Excel file",
            description = "Import STUDENT, LECTURER, or DEAN accounts. " +
                    "LECTURER and DEAN require majorId. " +
                    "Auto-generates password if not provided."
    )
    public ResponseEntity<?> importAccounts(
            @Parameter(description = "Excel file with account data", required = true)
            @RequestPart("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "File cannot be empty"));
        }

        try {
            List<Account> importedAccounts = accountService.importAccountsFromExcel(file);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Imported " + importedAccounts.size() + " account(s) successfully",
                    "data", importedAccounts
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error importing file: " + e.getMessage()
                    ));
        }
    }

    @GetMapping
    @Operation(summary = "Get all accounts with pagination and filters")
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

    @PutMapping("/update")
    @Operation(summary = "Update current account profile")
    public ResponseDto<AccountDetailResponse> updateAccount(
            @RequestBody UpdateAccountDto request,
            Authentication authentication) {
        return accountService.updateAccount(request, authentication);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current account info")
    public ResponseDto<AccountDto> me(Authentication authentication) {
        return accountService.getMe(authentication);
    }

    @PostMapping("/forgot-password")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Forgot password - Send temporary password via email")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        accountService.forgotPasswordSimple(req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "If email exists, temporary password has been sent"
        ));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for authenticated user")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest req,
            Authentication auth) {
        accountService.changePassword(req, auth);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password changed successfully"
        ));
    }
}