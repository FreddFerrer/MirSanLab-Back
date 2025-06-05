package com.mirsanlab.backend.dto;

import java.time.LocalDateTime;

// Respuesta al paciente
public record ResultadoResponseDto(
        Long id,
        String archivoUrl,
        LocalDateTime creadoEn
) {}
