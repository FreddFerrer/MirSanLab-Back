package com.mirsanlab.backend.mapper;

import com.mirsanlab.backend.dto.TurnoAdminDto;
import com.mirsanlab.backend.dto.TurnoRequestDto;
import com.mirsanlab.backend.dto.TurnoResponseDto;
import com.mirsanlab.backend.entity.Turno;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TurnoMapper {

    Turno toEntity(TurnoRequestDto dto);

    TurnoResponseDto toDto(Turno turno);

    @Mapping(source = "paciente.nombre", target = "nombrePaciente")
    TurnoAdminDto toAdminDto(Turno turno);
}
