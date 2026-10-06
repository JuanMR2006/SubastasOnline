package com.subastas.online.mapper;

import com.subastas.online.domain.EstadoSubasta;
import com.subastas.online.domain.Subasta;
import com.subastas.online.dto.response.SubastaResponse;
import com.subastas.online.dto.response.SubastaResumenResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", imports = { LocalDateTime.class, EstadoSubasta.class })
public interface SubastaMapper {

    @Mapping(target = "articuloId", source = "articulo.id")
    @Mapping(target = "articuloNombre", source = "articulo.nombre")
    @Mapping(target = "articuloDescripcion", expression = "java(subasta.getArticulo().describir())")
    @Mapping(target = "vendedorId", source = "vendedor.id")
    @Mapping(target = "vendedorNombre", source = "vendedor.nombre")
    @Mapping(target = "ganadorId", source = "ganador.id")
    @Mapping(target = "ganadorNombre", source = "ganador.nombre")
    @Mapping(target = "inicio", expression = "java(subasta.getPeriodo().inicio())")
    @Mapping(target = "fin", expression = "java(subasta.getPeriodo().fin())")
    @Mapping(target = "estado", expression = "java(subasta.getEstado().name())")
    @Mapping(target = "ofertaActual", expression = "java(subasta.ofertaActual())")
    @Mapping(target = "proximaOfertaMinima", expression = "java(subasta.ofertaMinimaSiguiente())")
    @Mapping(target = "cantidadParticipantes", expression = "java(subasta.participantes().size())")
    @Mapping(target = "cantidadPujas", expression = "java(subasta.getPujas().size())")
    @Mapping(target = "tiempoRestanteSegundos", expression = "java(subasta.tiempoRestante(LocalDateTime.now()).toSeconds())")
    @Mapping(target = "precioFinal", expression = "java(subasta.getEstado() == EstadoSubasta.FINALIZADA ? subasta.getPrecioFinal() : null)")
    @Mapping(target = "comision", expression = "java(subasta.getEstado() == EstadoSubasta.FINALIZADA ? subasta.comision() : null)")
    SubastaResponse toResponse(Subasta subasta);

    @Mapping(target = "articuloId", source = "articulo.id")
    @Mapping(target = "articuloNombre", source = "articulo.nombre")
    @Mapping(target = "vendedorId", source = "vendedor.id")
    @Mapping(target = "vendedorNombre", source = "vendedor.nombre")
    @Mapping(target = "ofertaActual", expression = "java(subasta.ofertaActual())")
    @Mapping(target = "cantidadParticipantes", expression = "java(subasta.participantes().size())")
    @Mapping(target = "tiempoRestanteSegundos", expression = "java(subasta.tiempoRestante(LocalDateTime.now()).toSeconds())")
    @Mapping(target = "estado", expression = "java(subasta.getEstado().name())")
    SubastaResumenResponse toResumen(Subasta subasta);

    List<SubastaResumenResponse> toResumenList(List<Subasta> subastas);
}
