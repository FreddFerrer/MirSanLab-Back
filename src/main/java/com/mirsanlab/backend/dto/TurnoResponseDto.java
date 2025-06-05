package com.mirsanlab.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

// Respuesta general de turno
public record TurnoResponseDto(
        Long id,
        LocalDate fecha,
        LocalTime hora,
        String estado
) {}
