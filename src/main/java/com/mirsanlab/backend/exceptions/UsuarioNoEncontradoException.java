package com.mirsanlab.backend.exceptions;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException(String email) {
        super("No se encontro un usuario con el email: " + email);
    }

    public UsuarioNoEncontradoException(Long id) {
        super("No se encontro un usuario con el id: " + id);
    }
}
