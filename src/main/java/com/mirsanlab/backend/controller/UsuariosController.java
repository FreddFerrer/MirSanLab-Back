package com.mirsanlab.backend.controller;

import com.mirsanlab.backend.dto.UsuarioResponseDto;
import com.mirsanlab.backend.entity.Usuario;
import com.mirsanlab.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuariosController {

    private final UsuarioService usuarioService;

    @GetMapping("/buscar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDto>> buscarUsuarios(@RequestParam("query") String query) {
        return ResponseEntity.ok(usuarioService.buscarUsuarios(query));
    }

}
