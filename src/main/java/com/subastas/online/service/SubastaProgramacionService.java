package com.subastas.online.service;

import com.subastas.online.domain.EstadoSubasta;
import com.subastas.online.repository.SubastaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de las tareas programadas (regla 8). Sin @Transactional a propósito:
 * cada subasta se procesa en su propia transacción (SubastaService), así una
 * que falle no deshace el resto.
 */
@Service
public class SubastaProgramacionService {

    private static final Logger log = LoggerFactory.getLogger(SubastaProgramacionService.class);

    private final SubastaRepository subastaRepository;
    private final SubastaService subastaService;

    public SubastaProgramacionService(SubastaRepository subastaRepository, SubastaService subastaService) {
        this.subastaRepository = subastaRepository;
        this.subastaService = subastaService;
    }

    public void procesar(LocalDateTime ahora) {
        activarProgramadas(ahora);
        cerrarVencidas(ahora);
    }

    /** PROGRAMADA cuyo inicio ya llegó -> ACTIVA. */
    public int activarProgramadas(LocalDateTime ahora) {
        List<Long> ids = subastaRepository.findIdsPorEstadoConInicioHasta(EstadoSubasta.PROGRAMADA, ahora);
        int procesadas = 0;
        for (Long id : ids) {
            try {
                subastaService.activarProgramada(id);
                procesadas++;
            } catch (RuntimeException ex) {
                log.warn("No se pudo activar la subasta {}: {}", id, ex.getMessage());
            }
        }
        if (procesadas > 0) {
            log.info("Subastas activadas: {}", procesadas);
        }
        return procesadas;
    }

    /** ACTIVA cuyo fin ya pasó -> FINALIZADA o DESIERTA. */
    public int cerrarVencidas(LocalDateTime ahora) {
        List<Long> ids = subastaRepository.findIdsPorEstadoConFinHasta(EstadoSubasta.ACTIVA, ahora);
        int procesadas = 0;
        for (Long id : ids) {
            try {
                subastaService.cerrarVencida(id);
                procesadas++;
            } catch (RuntimeException ex) {
                log.warn("No se pudo cerrar la subasta {}: {}", id, ex.getMessage());
            }
        }
        if (procesadas > 0) {
            log.info("Subastas cerradas: {}", procesadas);
        }
        return procesadas;
    }
}
