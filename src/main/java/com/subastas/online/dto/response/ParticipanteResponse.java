package com.subastas.online.dto.response;

import java.time.LocalDateTime;

public record ParticipanteResponse(
        Long usuarioId,
        String nombre,
        double mejorOferta,
        int cantidadPujas,
        LocalDateTime fechaUltimaPuja) {
}
