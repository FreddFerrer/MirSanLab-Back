package com.mirsanlab.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record TurnoAdminResponseDto(
        Long id,
        LocalDate fecha,
        LocalTime hora,
        String estado,
        String nombrePaciente,
        String emailPaciente,
        String telefonoPaciente
) {}
