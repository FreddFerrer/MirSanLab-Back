package com.mirsanlab.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DisponibilidadDiaDto(
        LocalDate fecha,
        List<LocalTime> horariosDisponibles
) {}
