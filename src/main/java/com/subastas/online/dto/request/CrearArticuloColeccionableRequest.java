package com.subastas.online.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearArticuloColeccionableRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 500) String descripcion,
        @NotNull Long duenoId,
        @NotBlank @Size(max = 120) String autor,
        @NotNull @Positive Integer anioOrigen) {
}
