package com.mirsanlab.backend.exceptions;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException(String email) {
        super("No se encontró un usuario con el email: " + email);
    }
    public UsuarioNoEncontradoException(Long id) {
        super("No se encontró un usuario con el email: " + id);
    }
}
