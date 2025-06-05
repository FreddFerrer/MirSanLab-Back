package com.mirsanlab.backend.service;

import com.mirsanlab.backend.dto.ResultadoResponseDto;
import com.mirsanlab.backend.entity.Usuario;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ResultadoService {
    void subirResultado(Long pacienteId, MultipartFile archivo);
    Page<ResultadoResponseDto> obtenerResultadosPaciente(Long pacienteId, int page, int size);
    ResponseEntity<Resource> descargarResultado(Long resultadoId, Usuario paciente);

}
