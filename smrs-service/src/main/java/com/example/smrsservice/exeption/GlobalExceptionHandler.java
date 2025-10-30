package com.example.smrsservice.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Map;

public class GlobalExceptionHandler {
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<?> handleAccountNotFound(AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "ACCOUNT_NOT_FOUND",
                        "message", ex.getMessage()
                ));
    }

    // 400: dữ liệu không hợp lệ (update, change password, ...)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", "BAD_REQUEST",
                        "message", ex.getMessage()
                ));
    }

    // 401: chưa auth / token sai
    @ExceptionHandler({ BadCredentialsException.class })
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", "UNAUTHORIZED",
                        "message", ex.getMessage()
                ));
    }

    // ví dụ: nhập mật khẩu cũ sai khi change password
    @ExceptionHandler(OldPasswordNotMatchException.class)
    public ResponseEntity<?> handleOldPasswordNotMatch(OldPasswordNotMatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "OLD_PASSWORD_NOT_MATCH",
                        "message", ex.getMessage()
                ));
    }

    // fallback: lỗi khác
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOther(Exception ex) {
        // log ra để dev coi
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "INTERNAL_ERROR",
                        "message", "Lỗi hệ thống"
                ));
    }


}
