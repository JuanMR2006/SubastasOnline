package com.subastas.online.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearPujaRequest(
        @NotNull Long postorId,
        @NotNull @Positive Double monto) {
}
