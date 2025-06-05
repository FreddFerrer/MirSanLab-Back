package com.mirsanlab.backend.dto;

// Subida de resultado (admin)
public record ResultadoRequestDto(
        Long turnoId,
        Long pacienteId
        // el archivo vendrá como MultipartFile, fuera del DTO
) {}
