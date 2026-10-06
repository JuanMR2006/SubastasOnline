package com.subastas.online.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record CrearSubastaRequest(
        @NotNull Long articuloId,
        @NotNull Long vendedorId,
        @NotNull @Positive Double precioBase,
        @NotNull @Positive Double incrementoMinimo,
        @NotNull @FutureOrPresent LocalDateTime inicio,
        @NotNull @Future LocalDateTime fin) {
}
