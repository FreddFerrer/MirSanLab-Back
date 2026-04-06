package com.mirsanlab.backend.exceptions;

public class CorreoInvalidoException extends RuntimeException {
    public CorreoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
