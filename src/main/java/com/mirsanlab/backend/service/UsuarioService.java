package com.mirsanlab.backend.service;

import com.mirsanlab.backend.dto.UsuarioLoginDto;
import com.mirsanlab.backend.dto.UsuarioLoginResponseDto;
import com.mirsanlab.backend.dto.UsuarioRegistroDto;
import com.mirsanlab.backend.dto.UsuarioResponseDto;

import java.util.List;

public interface UsuarioService {
    void registrarPaciente(UsuarioRegistroDto dto);
    UsuarioLoginResponseDto login(UsuarioLoginDto dto);
    List<UsuarioResponseDto> buscarUsuarios(String query);
}
