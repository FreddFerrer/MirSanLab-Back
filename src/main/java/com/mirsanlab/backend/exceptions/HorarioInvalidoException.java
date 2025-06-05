package com.mirsanlab.backend.exceptions;

import java.time.LocalTime;

public class HorarioInvalidoException extends RuntimeException {
    public HorarioInvalidoException(LocalTime hora) {
        super("La hora " + hora + " no es válida. Debe elegir una hora permitida.");
    }
}
