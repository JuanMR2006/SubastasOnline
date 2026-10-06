package com.subastas.online.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad intermedia de la relación N:M entre Usuario y Subasta.
 * Solo se crea desde Subasta.ofertar(), que es quien valida las reglas.
 */
@Entity
@Table(name = "pujas")
public class Puja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subasta_id", nullable = false)
    private Subasta subasta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "postor_id", nullable = false)
    private Usuario postor;

    @Column(nullable = false)
    private double monto;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    protected Puja() {
    }

    Puja(Subasta subasta, Usuario postor, double monto, LocalDateTime fechaHora) {
        if (subasta == null)
            throw new IllegalArgumentException("Subasta obligatoria");
        if (postor == null)
            throw new IllegalArgumentException("Postor obligatorio");
        if (monto <= 0)
            throw new IllegalArgumentException("El monto debe ser positivo");
        if (fechaHora == null)
            throw new IllegalArgumentException("Fecha y hora obligatorias");
        this.subasta = subasta;
        this.postor = postor;
        this.monto = monto;
        this.fechaHora = fechaHora;
    }

    public Long getId() {
        return id;
    }

    public Subasta getSubasta() {
        return subasta;
    }

    public Usuario getPostor() {
        return postor;
    }

    public double getMonto() {
        return monto;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}
