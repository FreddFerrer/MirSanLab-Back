package com.mirsanlab.backend.controller;

import com.mirsanlab.backend.dto.ResultadoResponseDto;
import com.mirsanlab.backend.entity.Usuario;
import com.mirsanlab.backend.service.ResultadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/resultados")
@RequiredArgsConstructor
public class ResultadosController {
    private final ResultadoService resultadoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> subirResultado(
            @RequestParam(value = "pacienteId") Optional<Long> pacienteId,
            @RequestParam(value = "emailDestino", required = false) String emailDestino,
            @RequestParam(value = "correoDestino", required = false) String correoDestino,
            @RequestParam("archivo") MultipartFile archivo) {

        if (pacienteId.isPresent()) {
            resultadoService.subirResultado(pacienteId.get(), archivo);
        } else {
            String destino = StringUtils.hasText(emailDestino) ? emailDestino : correoDestino;
            resultadoService.subirResultadoPorEmail(destino, archivo);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping()
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Page<ResultadoResponseDto>> obtenerResultados(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ResultadoResponseDto> resultados = resultadoService.obtenerResultadosPaciente(usuario.getId(), page, size);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/{id}/descargar")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Resource> descargarResultado(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        return resultadoService.descargarResultado(id, usuario);
    }
}
