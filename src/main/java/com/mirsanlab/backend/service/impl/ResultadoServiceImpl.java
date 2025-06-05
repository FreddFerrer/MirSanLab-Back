package com.mirsanlab.backend.service.impl;

import com.mirsanlab.backend.dto.ResultadoResponseDto;
import com.mirsanlab.backend.entity.Resultado;
import com.mirsanlab.backend.entity.Usuario;
import com.mirsanlab.backend.exceptions.ArchivoInvalidoException;
import com.mirsanlab.backend.exceptions.ResultadoNoEncontradoException;
import com.mirsanlab.backend.exceptions.UsuarioNoEncontradoException;
import com.mirsanlab.backend.mapper.ResultadoMapper;
import com.mirsanlab.backend.repository.ResultadoRepository;
import com.mirsanlab.backend.repository.UsuarioRepository;
import com.mirsanlab.backend.service.ResultadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResultadoServiceImpl implements ResultadoService {
    private final UsuarioRepository usuarioRepository;
    private final ResultadoRepository resultadoRepository;
    private final ResultadoMapper resultadoMapper;

    @Override
    public void subirResultado(Long pacienteId, MultipartFile archivo) {
        // Validar existencia del paciente
        Usuario paciente = usuarioRepository.findById(pacienteId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(pacienteId));

        // Validar que se haya enviado exactamente un archivo
        if (archivo == null || archivo.isEmpty()) {
            throw new ArchivoInvalidoException("Debe enviar un archivo PDF.");
        }

        // Validar extension y tipo MIME
        String nombreOriginal = archivo.getOriginalFilename();
        String contentType = archivo.getContentType();

        if (nombreOriginal == null || !nombreOriginal.toLowerCase().endsWith(".pdf")) {
            throw new ArchivoInvalidoException("El archivo debe tener extensión .pdf.");
        }

        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new ArchivoInvalidoException("El archivo debe ser de tipo PDF (application/pdf).");
        }

        try {
            // Crear nombre unico para guardar
            String nombreArchivo = "resultado_" + pacienteId + "_" + System.currentTimeMillis() + ".pdf";
            Path path = Paths.get("archivos/" + nombreArchivo);
            Files.createDirectories(path.getParent());
            Files.write(path, archivo.getBytes());

            Resultado resultado = Resultado.builder()
                    .archivoUrl(path.toString())
                    .paciente(paciente)
                    .creadoEn(LocalDateTime.now())
                    .build();

            resultadoRepository.save(resultado);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }
    }

    @Override
    public Page<ResultadoResponseDto> obtenerResultadosPaciente(Long pacienteId, int page, int size) {
        Usuario paciente = usuarioRepository.findById(pacienteId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(pacienteId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("creadoEn").descending());

        return resultadoRepository.findByPaciente(paciente, pageable)
                .map(resultadoMapper::toDto);
    }

    @Override
    public ResponseEntity<Resource> descargarResultado(Long resultadoId, Usuario paciente) {
        Resultado resultado = resultadoRepository.findById(resultadoId)
                .orElseThrow(() -> new ResultadoNoEncontradoException(resultadoId));

        if (!resultado.getPaciente().getId().equals(paciente.getId())) {
            throw new AccessDeniedException("No tenés permiso para acceder a este archivo.");
        }

        Path archivoPath = Paths.get(resultado.getArchivoUrl());

        if (!Files.exists(archivoPath)) {
            throw new ResultadoNoEncontradoException(resultadoId);
        }

        Resource recurso = new FileSystemResource(archivoPath);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + archivoPath.getFileName().toString() + "\"")
                .body(recurso);
    }

}
