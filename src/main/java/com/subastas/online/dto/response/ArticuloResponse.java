package com.subastas.online.dto.response;

/** Los campos propios de cada subtipo vienen en null cuando no aplican. */
public record ArticuloResponse(
        Long id,
        String tipo,
        String nombre,
        String descripcion,
        String descripcionCompleta,
        Long duenoId,
        String duenoNombre,
        String estado,
        double porcentajeComision,
        String marca,
        String modelo,
        String estadoUso,
        String autor,
        Integer anioOrigen) {
}
