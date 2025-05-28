package com.mirsanlab.backend.exceptions;

import java.time.LocalDate;

public class FechaInvalidaException extends RuntimeException {
    public FechaInvalidaException(LocalDate fecha) {
        super("La fecha " + fecha + " no está permitida para reservar un turno.");
    }
}
