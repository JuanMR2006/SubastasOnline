package com.subastas.online.mapper;

import com.subastas.online.domain.Articulo;
import com.subastas.online.domain.ArticuloColeccionable;
import com.subastas.online.domain.ArticuloElectronico;
import com.subastas.online.dto.response.ArticuloResponse;
import org.hibernate.Hibernate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ArticuloMapper {

    /**
     * Despacha según el subtipo real. Primero se "desenvuelve" el proxy LAZY de
     * Hibernate, porque un proxy de Articulo no es instanceof de sus subclases.
     */
    default ArticuloResponse toResponse(Articulo articulo) {
        if (articulo == null) {
            return null;
        }
        Object real = Hibernate.unproxy(articulo);
        if (real instanceof ArticuloElectronico electronico) {
            return electronicoToResponse(electronico);
        }
        if (real instanceof ArticuloColeccionable coleccionable) {
            return coleccionableToResponse(coleccionable);
        }
        throw new IllegalArgumentException("Tipo de artículo no soportado: " + real.getClass().getSimpleName());
    }

    List<ArticuloResponse> toResponseList(List<Articulo> articulos);

    @Mapping(target = "tipo", constant = "ELECTRONICO")
    @Mapping(target = "descripcionCompleta", expression = "java(articulo.describir())")
    @Mapping(target = "duenoId", source = "dueno.id")
    @Mapping(target = "duenoNombre", source = "dueno.nombre")
    @Mapping(target = "estado", expression = "java(articulo.getEstado().name())")
    @Mapping(target = "porcentajeComision", expression = "java(articulo.porcentajeComision())")
    @Mapping(target = "autor", ignore = true)
    @Mapping(target = "anioOrigen", ignore = true)
    ArticuloResponse electronicoToResponse(ArticuloElectronico articulo);

    @Mapping(target = "tipo", constant = "COLECCIONABLE")
    @Mapping(target = "descripcionCompleta", expression = "java(articulo.describir())")
    @Mapping(target = "duenoId", source = "dueno.id")
    @Mapping(target = "duenoNombre", source = "dueno.nombre")
    @Mapping(target = "estado", expression = "java(articulo.getEstado().name())")
    @Mapping(target = "porcentajeComision", expression = "java(articulo.porcentajeComision())")
    @Mapping(target = "marca", ignore = true)
    @Mapping(target = "modelo", ignore = true)
    @Mapping(target = "estadoUso", ignore = true)
    ArticuloResponse coleccionableToResponse(ArticuloColeccionable articulo);
}
