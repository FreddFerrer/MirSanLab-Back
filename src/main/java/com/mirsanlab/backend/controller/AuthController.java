package com.mirsanlab.backend.controller;

import com.mirsanlab.backend.dto.UsuarioLoginDto;
import com.mirsanlab.backend.dto.UsuarioLoginResponseDto;
import com.mirsanlab.backend.dto.UsuarioRegistroDto;
import com.mirsanlab.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@Valid @RequestBody UsuarioRegistroDto dto) {
        usuarioService.registrarPaciente(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioLoginResponseDto> login(@RequestBody UsuarioLoginDto dto) {
        UsuarioLoginResponseDto response = usuarioService.login(dto);
        return ResponseEntity.ok(response);
    }
}
