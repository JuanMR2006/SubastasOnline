package com.subastas.online.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RangoFechasTest {

    private final LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Test
    void finAnteriorAlInicioEsInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new RangoFechas(base, base.minusMinutes(1)));
    }

    @Test
    void finIgualAlInicioEsInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new RangoFechas(base, base));
    }

    @Test
    void fechasNulasSonInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> new RangoFechas(null, base));
        assertThrows(IllegalArgumentException.class, () -> new RangoFechas(base, null));
    }

    @Test
    void contieneIncluyeLosExtremosYExcluyeLoDemas() {
        RangoFechas rango = new RangoFechas(base, base.plusHours(2));
        assertTrue(rango.contiene(base));
        assertTrue(rango.contiene(base.plusHours(1)));
        assertTrue(rango.contiene(base.plusHours(2)));
        assertFalse(rango.contiene(base.minusSeconds(1)));
        assertFalse(rango.contiene(base.plusHours(2).plusSeconds(1)));
        assertFalse(rango.contiene(null));
    }
}
