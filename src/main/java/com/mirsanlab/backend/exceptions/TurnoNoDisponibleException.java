package com.mirsanlab.backend.exceptions;

import java.time.LocalDate;
import java.time.LocalTime;

public class TurnoNoDisponibleException extends RuntimeException {
    public TurnoNoDisponibleException(LocalDate fecha, LocalTime hora) {
        super("El turno para el " + fecha + " a las " + hora + " ya fue reservado.");
    }
}