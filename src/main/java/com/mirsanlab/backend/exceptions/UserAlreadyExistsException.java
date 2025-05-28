package com.mirsanlab.backend.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("El email ya está en uso: " + email);
    }
}
