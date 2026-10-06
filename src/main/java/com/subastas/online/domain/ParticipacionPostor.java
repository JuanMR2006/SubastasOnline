package com.subastas.online.domain;

import java.time.LocalDateTime;

/** Resumen de la participación de un postor en una subasta (no es entidad). */
public record ParticipacionPostor(
        Usuario postor,
        double mejorOferta,
        int cantidadPujas,
        LocalDateTime fechaUltimaPuja) {
}
