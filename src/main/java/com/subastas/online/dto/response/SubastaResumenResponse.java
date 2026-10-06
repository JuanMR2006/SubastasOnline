package com.subastas.online.dto.response;

public record SubastaResumenResponse(
        Long id,
        Long articuloId,
        String articuloNombre,
        Long vendedorId,
        String vendedorNombre,
        double precioBase,
        double ofertaActual,
        int cantidadParticipantes,
        long tiempoRestanteSegundos,
        String estado) {
}
