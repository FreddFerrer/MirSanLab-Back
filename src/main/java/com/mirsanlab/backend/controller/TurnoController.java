package com.mirsanlab.backend.controller;

import com.mirsanlab.backend.dto.DisponibilidadDiaDto;
import com.mirsanlab.backend.dto.TurnoAdminResponseDto;
import com.mirsanlab.backend.dto.TurnoRequestDto;
import com.mirsanlab.backend.entity.Usuario;
import com.mirsanlab.backend.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {
    private final TurnoService turnoService;

    @GetMapping()
    public ResponseEntity<List<DisponibilidadDiaDto>> obtenerDisponibilidad(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size) {
        return ResponseEntity.ok(turnoService.obtenerTurnosDisponibles(page, size));
    }

    @PostMapping
    public ResponseEntity<Void> crearTurno(@RequestBody TurnoRequestDto dto,
                                           @AuthenticationPrincipal Usuario usuario) {
        turnoService.crearTurno(usuario.getId(), dto);
        return ResponseEntity.ok().build(); // o created
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TurnoAdminResponseDto>> verTurnosPendientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(turnoService.obtenerTurnosPendientesParaAdmin(page, size));
    }


    @PutMapping("/{id}/realizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> marcarTurnoComoRealizado(@PathVariable Long id) {
        turnoService.marcarComoRealizado(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelarTurno(@PathVariable Long id) {
        turnoService.cancelarTurno(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/cancelar-paciente")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Void> cancelarTurnoPaciente(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario paciente) {
        turnoService.cancelarTurnoPorPaciente(id, paciente.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/proximo")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<TurnoAdminResponseDto> obtenerProximoTurno(
            @AuthenticationPrincipal Usuario paciente) {

        Optional<TurnoAdminResponseDto> turno = turnoService.obtenerProximoTurnoPaciente(paciente.getId());

        return turno.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

}
