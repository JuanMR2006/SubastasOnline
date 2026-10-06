package com.subastas.online.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "articulos_electronicos")
public class ArticuloElectronico extends Articulo {

    @Column(nullable = false, length = 80)
    private String marca;

    @Column(nullable = false, length = 80)
    private String modelo;

    @Column(name = "estado_uso", nullable = false, length = 80)
    private String estadoUso;

    protected ArticuloElectronico() {
    }

    public ArticuloElectronico(String nombre, String descripcion, String marca, String modelo, String estadoUso,
            Usuario dueno) {
        super(nombre, descripcion, dueno);
        if (marca == null || marca.isBlank())
            throw new IllegalArgumentException("La marca es obligatoria");
        if (modelo == null || modelo.isBlank())
            throw new IllegalArgumentException("El modelo es obligatorio");
        if (estadoUso == null || estadoUso.isBlank())
            throw new IllegalArgumentException("El estado de uso es obligatorio");
        this.marca = marca;
        this.modelo = modelo;
        this.estadoUso = estadoUso;
    }

    @Override
    public String describir() {
        return getNombre() + " - " + marca + " " + modelo + " (" + estadoUso + ")";
    }

    // NO sobrescribe porcentajeComision(): usa el del padre (5 %).

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public String getEstadoUso() {
        return estadoUso;
    }
}
