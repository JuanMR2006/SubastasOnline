package com.subastas.online.repository;

import com.subastas.online.domain.EstadoSubasta;
import com.subastas.online.domain.Subasta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubastaRepository extends JpaRepository<Subasta, Long> {

    List<Subasta> findByEstado(EstadoSubasta estado);

    boolean existsByArticuloIdAndEstadoIn(Long articuloId, Collection<EstadoSubasta> estados);

    // Bloqueo de fila para que dos ofertas (o una oferta y un cierre) no se pisen.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Subasta s where s.id = :id")
    Optional<Subasta> findByIdConBloqueo(@Param("id") Long id);

    @Query("select s.id from Subasta s where s.estado = :estado and s.periodo.inicio <= :ahora")
    List<Long> findIdsPorEstadoConInicioHasta(@Param("estado") EstadoSubasta estado,
            @Param("ahora") LocalDateTime ahora);

    @Query("select s.id from Subasta s where s.estado = :estado and s.periodo.fin <= :ahora")
    List<Long> findIdsPorEstadoConFinHasta(@Param("estado") EstadoSubasta estado,
            @Param("ahora") LocalDateTime ahora);
}
