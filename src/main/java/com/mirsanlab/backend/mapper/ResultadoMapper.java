package com.mirsanlab.backend.mapper;

import com.mirsanlab.backend.dto.ResultadoResponseDto;
import com.mirsanlab.backend.entity.Resultado;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResultadoMapper {

    ResultadoResponseDto toDto(Resultado resultado);
}