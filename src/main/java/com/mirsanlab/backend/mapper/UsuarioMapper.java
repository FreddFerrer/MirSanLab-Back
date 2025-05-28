package com.mirsanlab.backend.mapper;

import com.mirsanlab.backend.dto.UsuarioLoginResponseDto;
import com.mirsanlab.backend.dto.UsuarioRegistroDto;
import com.mirsanlab.backend.dto.UsuarioResponseDto;
import com.mirsanlab.backend.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    Usuario toEntity(UsuarioRegistroDto dto);

    @Mapping(source = "usuario.rol", target = "rol")
    @Mapping(source = "usuario.nombre", target = "nombre")
    @Mapping(source = "token", target = "token")
    UsuarioLoginResponseDto toLoginResponse(Usuario usuario, String token);
    UsuarioResponseDto toDto(Usuario usuario);

    List<UsuarioResponseDto> toDtoList(List<Usuario> usuarios);
}
