package com.mirsanlab.backend.dto;

// Respuesta al login (contiene JWT)
public record UsuarioLoginResponseDto(
        String token,
        String rol,
        String nombre
) {}

