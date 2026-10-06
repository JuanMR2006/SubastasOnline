package com.subastas.online.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String correo;

    @Column(nullable = false, length = 80)
    private String ciudad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    protected Usuario() {
    }

    public Usuario(String nombre, String correo, String ciudad, Rol rol) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        if (ciudad == null || ciudad.isBlank()) {
            throw new IllegalArgumentException("La ciudad es obligatoria");
        }
        this.nombre = nombre;
        this.correo = correo;
        this.ciudad = ciudad;
        this.rol = (rol == null) ? Rol.USUARIO : rol;
    }

    public boolean esAdministrador() {
        return this.rol == Rol.ADMINISTRADOR;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getCiudad() {
        return ciudad;
    }

    public Rol getRol() {
        return rol;
    }

    // Igualdad por id (funciona también con proxies LAZY de Hibernate).
    // Si el usuario aún no se persistió (id nulo) solo es igual a sí mismo.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Usuario otro)) {
            return false;
        }
        return id != null && id.equals(otro.getId());
    }

    @Override
    public int hashCode() {
        return Usuario.class.hashCode();
    }
}
