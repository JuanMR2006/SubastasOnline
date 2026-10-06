package com.subastas.online.dto.response;

import java.time.LocalDateTime;

public record PujaResponse(
        Long id,
        Long subastaId,
        Long postorId,
        String postorNombre,
        double monto,
        LocalDateTime fechaHora) {
}
