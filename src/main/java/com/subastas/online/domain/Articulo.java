package com.subastas.online.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "articulos")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Articulo {

    public static final double COMISION_BASE = 0.05;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dueno_id", nullable = false)
    private Usuario dueno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoArticulo estado;

    protected Articulo() {
    }

    protected Articulo(String nombre, String descripcion, Usuario dueno) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del artículo es obligatorio");
        if (descripcion == null || descripcion.isBlank())
            throw new IllegalArgumentException("La descripción del artículo es obligatoria");
        if (dueno == null)
            throw new IllegalArgumentException("El dueño del artículo es obligatorio");

        this.nombre = nombre;
        this.descripcion = descripcion;
        this.dueno = dueno;
        this.estado = EstadoArticulo.DISPONIBLE;
    }

    /** Cada subtipo describe el artículo a su manera. */
    public abstract String describir();

    /** Porcentaje de comisión por defecto; los subtipos pueden sobrescribirlo. */
    public double porcentajeComision() {
        return COMISION_BASE;
    }

    /**
     * Polimorfismo: invoca this.porcentajeComision() y el despacho dinámico
     * ejecuta la versión del tipo real del objeto (5 % u 8 %).
     */
    public double calcularComision(double precioFinal) {
        if (precioFinal < 0) {
            throw new IllegalArgumentException("El precio final no puede ser negativo");
        }
        return precioFinal * porcentajeComision();
    }

    public void cambiarEstado(EstadoArticulo nuevo) {
        if (nuevo == null) {
            throw new IllegalArgumentException("El nuevo estado es obligatorio");
        }
        boolean permitida = switch (this.estado) {
            case DISPONIBLE -> nuevo == EstadoArticulo.EN_SUBASTA;
            case EN_SUBASTA -> nuevo == EstadoArticulo.DISPONIBLE || nuevo == EstadoArticulo.VENDIDO;
            case VENDIDO -> false;
        };
        if (!permitida) {
            throw new IllegalStateException(
                    "Transición de estado no permitida para el artículo: " + this.estado + " -> " + nuevo);
        }
        this.estado = nuevo;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Usuario getDueno() {
        return dueno;
    }

    public EstadoArticulo getEstado() {
        return estado;
    }
}
