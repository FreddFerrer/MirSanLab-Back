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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resultados")
@RequiredArgsConstructor
public class ResultadosController {
    private final ResultadoService resultadoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> subirResultado(
            @RequestParam("pacienteId") Long pacienteId,
            @RequestParam("archivo") MultipartFile archivo) {

        resultadoService.subirResultado(pacienteId, archivo);
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
