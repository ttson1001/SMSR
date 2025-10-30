package com.example.smrsservice.exeption;

public class OldPasswordNotMatchException extends RuntimeException {
    public OldPasswordNotMatchException(String message) {
        super(message);
    }

    public OldPasswordNotMatchException() {
        super("Mật khẩu cũ không đúng");
    }
}
