package com.mirsanlab.backend.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Email o contraseña inválidos.");
    }
}
