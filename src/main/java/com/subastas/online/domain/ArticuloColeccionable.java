package com.subastas.online.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "articulos_coleccionables")
public class ArticuloColeccionable extends Articulo {

    private static final double COMISION_COLECCIONABLE = 0.08;

    @Column(nullable = false, length = 120)
    private String autor;

    @Column(name = "anio_origen", nullable = false)
    private int anioOrigen;

    protected ArticuloColeccionable() {
    }

    public ArticuloColeccionable(String nombre, String descripcion, String autor, int anioOrigen, Usuario dueno) {
        super(nombre, descripcion, dueno);
        if (autor == null || autor.isBlank())
            throw new IllegalArgumentException("El autor es obligatorio");
        if (anioOrigen <= 0)
            throw new IllegalArgumentException("El año de origen debe ser positivo");
        this.autor = autor;
        this.anioOrigen = anioOrigen;
    }

    @Override
    public String describir() {
        return getNombre() + " - " + autor + " (" + anioOrigen + ")";
    }

    @Override
    public double porcentajeComision() {
        return COMISION_COLECCIONABLE;
    }

    public String getAutor() {
        return autor;
    }

    public int getAnioOrigen() {
        return anioOrigen;
    }
}
