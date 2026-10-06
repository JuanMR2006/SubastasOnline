package com.subastas.online.mapper;

import com.subastas.online.domain.ParticipacionPostor;
import com.subastas.online.domain.Puja;
import com.subastas.online.dto.response.ParticipanteResponse;
import com.subastas.online.dto.response.PujaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PujaMapper {

    @Mapping(target = "subastaId", source = "subasta.id")
    @Mapping(target = "postorId", source = "postor.id")
    @Mapping(target = "postorNombre", source = "postor.nombre")
    PujaResponse toResponse(Puja puja);

    List<PujaResponse> toResponseList(List<Puja> pujas);

    @Mapping(target = "usuarioId", source = "participacion.postor.id")
    @Mapping(target = "nombre", source = "participacion.postor.nombre")
    ParticipanteResponse toParticipante(ParticipacionPostor participacion);

    List<ParticipanteResponse> toParticipanteList(List<ParticipacionPostor> participaciones);
}
