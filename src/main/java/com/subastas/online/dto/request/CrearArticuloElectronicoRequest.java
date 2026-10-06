package com.subastas.online.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearArticuloElectronicoRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 500) String descripcion,
        @NotNull Long duenoId,
        @NotBlank @Size(max = 80) String marca,
        @NotBlank @Size(max = 80) String modelo,
        @NotBlank @Size(max = 80) String estadoUso) {
}
