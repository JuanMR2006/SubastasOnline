package com.subastas.online.dto.response;

import java.time.LocalDateTime;

public record SubastaResponse(
        Long id,
        Long articuloId,
        String articuloNombre,
        String articuloDescripcion,
        Long vendedorId,
        String vendedorNombre,
        Long ganadorId,
        String ganadorNombre,
        double precioBase,
        double incrementoMinimo,
        LocalDateTime inicio,
        LocalDateTime fin,
        String estado,
        double ofertaActual,
        double proximaOfertaMinima,
        int cantidadParticipantes,
        int cantidadPujas,
        long tiempoRestanteSegundos,
        Double precioFinal,
        Double comision) {
}
