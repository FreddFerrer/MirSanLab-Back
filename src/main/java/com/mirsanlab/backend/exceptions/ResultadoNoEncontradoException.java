package com.mirsanlab.backend.exceptions;

public class ResultadoNoEncontradoException extends RuntimeException{
    public ResultadoNoEncontradoException(Long resutladoId){
        super("No se encontró el resultado " + resutladoId);
    }
}
