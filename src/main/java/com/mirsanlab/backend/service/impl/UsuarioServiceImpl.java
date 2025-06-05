package com.mirsanlab.backend.service.impl;

import com.mirsanlab.backend.dto.UsuarioLoginDto;
import com.mirsanlab.backend.dto.UsuarioLoginResponseDto;
import com.mirsanlab.backend.dto.UsuarioRegistroDto;
import com.mirsanlab.backend.dto.UsuarioResponseDto;
import com.mirsanlab.backend.entity.Usuario;
import com.mirsanlab.backend.exceptions.InvalidCredentialsException;
import com.mirsanlab.backend.exceptions.UserAlreadyExistsException;
import com.mirsanlab.backend.exceptions.UsuarioNoEncontradoException;
import com.mirsanlab.backend.mapper.UsuarioMapper;
import com.mirsanlab.backend.repository.UsuarioRepository;
import com.mirsanlab.backend.security.JwtService;
import com.mirsanlab.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public void registrarPaciente(UsuarioRegistroDto dto) {
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new UserAlreadyExistsException(dto.email());
        }

        Usuario nuevo = usuarioMapper.toEntity(dto);
        nuevo.setPassword(passwordEncoder.encode(dto.password()));
        nuevo.setRol(Usuario.Rol.PACIENTE);
        usuarioRepository.save(nuevo);
    }

    @Override
    public UsuarioLoginResponseDto login(UsuarioLoginDto dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
            );
        } catch (AuthenticationException e) {
            System.out.println("Fallo la autenticacion de " + dto.email());
            throw new InvalidCredentialsException();
        }

        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new UsuarioNoEncontradoException(dto.email()));

        System.out.println("Usuario encontrado: " + usuario.getEmail());

        String token = jwtService.generarToken(usuario);
        return usuarioMapper.toLoginResponse(usuario, token);
    }

    public List<UsuarioResponseDto> buscarUsuarios(String query) {
        List<Usuario> encontrados = usuarioRepository.buscarPorNombreEmailOTelefono(query);
        return usuarioMapper.toDtoList(encontrados);
    }

}
