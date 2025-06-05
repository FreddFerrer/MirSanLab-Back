package com.mirsanlab.backend.exceptions;

public class TurnoNoEncontradoException extends RuntimeException {
  public TurnoNoEncontradoException(Long id) {
    super("No se encontró el turno con ID: " + id);
  }
}
