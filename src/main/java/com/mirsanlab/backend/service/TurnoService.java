package com.mirsanlab.backend.service;

import com.mirsanlab.backend.dto.DisponibilidadDiaDto;
import com.mirsanlab.backend.dto.TurnoAdminResponseDto;
import com.mirsanlab.backend.dto.TurnoRequestDto;

import java.util.List;
import java.util.Optional;

public interface TurnoService {
    List<DisponibilidadDiaDto> obtenerTurnosDisponibles(int page, int size);
    void crearTurno(Long pacienteId, TurnoRequestDto dto);
    List<TurnoAdminResponseDto> obtenerTurnosPendientesParaAdmin(int page, int size);
    void marcarComoRealizado(Long turnoId);
    void cancelarTurno(Long turnoId);
    void cancelarTurnoPorPaciente(Long turnoId, Long pacienteId);
    Optional<TurnoAdminResponseDto> obtenerProximoTurnoPaciente(Long pacienteId);
}
