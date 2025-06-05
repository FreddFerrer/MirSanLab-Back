package com.mirsanlab.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

// Turno extendido (admin)
public record TurnoAdminDto(
        Long id,
        LocalDate fecha,
        LocalTime hora,
        String estado,
        String nombrePaciente
) {}
