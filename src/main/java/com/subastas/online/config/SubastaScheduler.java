package com.subastas.online.config;

import com.subastas.online.service.SubastaProgramacionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Tarea programada: solo invoca al servicio, sin lógica propia. */
@Component
public class SubastaScheduler {

    private final SubastaProgramacionService programacionService;

    public SubastaScheduler(SubastaProgramacionService programacionService) {
        this.programacionService = programacionService;
    }

    @Scheduled(fixedRate = 60_000)
    public void ejecutar() {
        programacionService.procesar(LocalDateTime.now());
    }
}
