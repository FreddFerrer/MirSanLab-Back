package com.mirsanlab.backend.exceptions;

public class EstadoInvalidoException extends RuntimeException {
  public EstadoInvalidoException(String mensaje) {
    super(mensaje);
  }
}
