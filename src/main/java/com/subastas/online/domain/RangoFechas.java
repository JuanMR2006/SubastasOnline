package com.subastas.online.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
public record RangoFechas(
        @Column(name = "fecha_inicio", nullable = false) LocalDateTime inicio,

        @Column(name = "fecha_fin", nullable = false) LocalDateTime fin) {
    public RangoFechas {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }
        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio");
        }
    }

    public boolean contiene(LocalDateTime fecha) {
        return fecha != null && !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }
}
