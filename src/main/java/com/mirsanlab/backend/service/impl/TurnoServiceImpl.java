package com.mirsanlab.backend.service.impl;

import com.mirsanlab.backend.dto.DisponibilidadDiaDto;
import com.mirsanlab.backend.dto.TurnoAdminResponseDto;
import com.mirsanlab.backend.dto.TurnoRequestDto;
import com.mirsanlab.backend.entity.Turno;
import com.mirsanlab.backend.entity.Usuario;
import com.mirsanlab.backend.exceptions.*;
import com.mirsanlab.backend.repository.TurnoRepository;
import com.mirsanlab.backend.repository.UsuarioRepository;
import com.mirsanlab.backend.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TurnoServiceImpl implements TurnoService {
    private final TurnoRepository turnoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public List<DisponibilidadDiaDto> obtenerTurnosDisponibles(int page, int size) {
        LocalDate hoy = LocalDate.now();
        LocalDate desde = hoy.plusDays(1);
        LocalDate hasta = desde.plusDays(30);

        List<LocalDate> fechasValidas = new ArrayList<>();

        for (LocalDate fecha = desde; !fecha.isAfter(hasta); fecha = fecha.plusDays(1)) {
            if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY && fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {
                fechasValidas.add(fecha);
            }
        }

        // Paginacin manual
        int start = page * size;
        int end = Math.min(start + size, fechasValidas.size());

        if (start >= fechasValidas.size()) {
            return List.of();
        }

        List<DisponibilidadDiaDto> disponibilidad = new ArrayList<>();
        for (LocalDate fecha : fechasValidas.subList(start, end)) {
            List<LocalTime> horarios = generarHorarios();
            List<Turno> turnosDelDia = turnoRepository.findByFechaAndEstado(fecha, Turno.Estado.PENDIENTE);
            List<LocalTime> ocupados = turnosDelDia.stream().map(Turno::getHora).toList();
            List<LocalTime> disponibles = horarios.stream()
                    .filter(h -> !ocupados.contains(h))
                    .toList();

            disponibilidad.add(new DisponibilidadDiaDto(fecha, disponibles));
        }

        return disponibilidad;
    }


    @Override
    public void crearTurno(Long pacienteId, TurnoRequestDto dto) {
        LocalDate hoy = LocalDate.now();
        LocalDate desde = hoy.plusDays(1);
        LocalDate hasta = desde.plusDays(30);

        // Validar que la fecha este dentro del rango de 30 dias y no sea sábado ni domingo
        if (dto.fecha().isBefore(desde) || dto.fecha().isAfter(hasta) ||
                dto.fecha().getDayOfWeek() == DayOfWeek.SATURDAY ||
                dto.fecha().getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new FechaInvalidaException(dto.fecha());
        }

        // Validar que la hora este entre los horarios permitidos (ej: 08:00 a 12:00 cada 30min)
        List<LocalTime> horariosPermitidos = generarHorarios();
        if (!horariosPermitidos.contains(dto.hora())) {
            throw new HorarioInvalidoException(dto.hora());
        }

        // Validar que el turno no este ocupado
        if (turnoRepository.existsByFechaAndHora(dto.fecha(), dto.hora())) {
            throw new TurnoNoDisponibleException(dto.fecha(), dto.hora());
        }

        Usuario paciente = usuarioRepository.findById(pacienteId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(pacienteId));

        Turno turno = Turno.builder()
                .fecha(dto.fecha())
                .hora(dto.hora())
                .paciente(paciente)
                .estado(Turno.Estado.PENDIENTE)
                .creadoEn(LocalDateTime.now())
                .build();

        turnoRepository.save(turno);
    }

    @Override
    public List<TurnoAdminResponseDto> obtenerTurnosPendientesParaAdmin(int page, int size) {
        LocalDate hoy = LocalDate.now();

        // Traemos todos los turnos pendientes desde hoy
        List<Turno> todos = turnoRepository
                .findByFechaGreaterThanEqualAndEstadoOrderByFechaAscHoraAsc(hoy, Turno.Estado.PENDIENTE);

        // Paginacion manual
        int start = page * size;
        int end = Math.min(start + size, todos.size());

        if (start >= todos.size()) {
            return List.of();
        }

        return todos.subList(start, end).stream()
                .map(turno -> new TurnoAdminResponseDto(
                        turno.getId(),
                        turno.getFecha(),
                        turno.getHora(),
                        turno.getEstado().name(),
                        turno.getPaciente().getNombre(),
                        turno.getPaciente().getEmail(),
                        turno.getPaciente().getTelefono()
                ))
                .toList();
    }



    @Override
    public void marcarComoRealizado(Long turnoId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new TurnoNoEncontradoException(turnoId));

        if (turno.getEstado() != Turno.Estado.PENDIENTE) {
            throw new EstadoInvalidoException("Solo se pueden marcar como realizados los turnos pendientes.");
        }

        turno.setEstado(Turno.Estado.REALIZADO);
        turnoRepository.save(turno);
    }

    @Override
    public void cancelarTurno(Long turnoId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new TurnoNoEncontradoException(turnoId));

        if (turno.getEstado() != Turno.Estado.PENDIENTE) {
            throw new EstadoInvalidoException("Solo se pueden cancelar turnos pendientes.");
        }

        turno.setEstado(Turno.Estado.CANCELADO);
        turnoRepository.save(turno);
    }

    @Override
    public void cancelarTurnoPorPaciente(Long turnoId, Long pacienteId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new TurnoNoEncontradoException(turnoId));

        // Validar que el turno le pertenezca al paciente logueado
        if (!turno.getPaciente().getId().equals(pacienteId)) {
            throw new AccessDeniedException("No tenés permiso para cancelar este turno.");
        }

        // Solo se pueden cancelar turnos pendientes
        if (turno.getEstado() != Turno.Estado.PENDIENTE) {
            throw new EstadoInvalidoException("Solo se pueden cancelar turnos pendientes.");
        }

        // Solo si faltan más de 24hs
        LocalDateTime fechaHoraTurno = LocalDateTime.of(turno.getFecha(), turno.getHora());
        if (fechaHoraTurno.isBefore(LocalDateTime.now().plusHours(24))) {
            throw new EstadoInvalidoException("Solo se pueden cancelar turnos con al menos 24hs de anticipación.");
        }

        turno.setEstado(Turno.Estado.CANCELADO);
        turnoRepository.save(turno);
    }

    @Override
    public Optional<TurnoAdminResponseDto> obtenerProximoTurnoPaciente(Long pacienteId) {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        List<Turno> turnos = turnoRepository.findProximoTurno(
                pacienteId,
                hoy,
                ahora,
                PageRequest.of(0, 1) // solo el primero
        );

        if (turnos.isEmpty()) {
            return Optional.empty();
        }

        Turno turno = turnos.get(0);

        return Optional.of(new TurnoAdminResponseDto(
                turno.getId(),
                turno.getFecha(),
                turno.getHora(),
                turno.getEstado().name(),
                turno.getPaciente().getNombre(),
                turno.getPaciente().getEmail(),
                turno.getPaciente().getTelefono()
        ));
    }




    private List<LocalTime> generarHorarios() {
        List<LocalTime> horarios = new ArrayList<>();
        LocalTime inicio = LocalTime.of(8, 0);
        LocalTime fin = LocalTime.of(12, 0);

        while (!inicio.isAfter(fin)) {
            horarios.add(inicio);
            inicio = inicio.plusMinutes(30);
        }

        return horarios;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void actualizarTurnosVencidos() {
        LocalDateTime ahora = LocalDateTime.now();

        // Obtener todos los turnos pendientes
        List<Turno> pendientes = turnoRepository.findByEstado(Turno.Estado.PENDIENTE);

        for (Turno turno : pendientes) {
            LocalDateTime fechaHoraTurno = LocalDateTime.of(turno.getFecha(), turno.getHora());

            // Si pasaron mas de 24hs desde la fecha y hora del turno
            if (fechaHoraTurno.plusHours(24).isBefore(ahora)) {
                turno.setEstado(Turno.Estado.REALIZADO);
            }
        }

        turnoRepository.saveAll(pendientes);
    }

}
