package com.mirsanlab.backend.exceptions;

public class EnvioCorreoException extends RuntimeException {
    public EnvioCorreoException(String mensaje, Throwable cause) {
        super(mensaje, cause);
    }
}
