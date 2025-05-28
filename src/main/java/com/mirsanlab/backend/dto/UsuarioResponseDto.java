package com.mirsanlab.backend.dto;

public record UsuarioResponseDto(
        Long id,
        String nombre,
        String email,
        String telefono
) {}

