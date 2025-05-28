package com.mirsanlab.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

// Crear o modificar turno
public record TurnoRequestDto(
        LocalDate fecha,
        LocalTime hora
) {}
