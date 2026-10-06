package com.subastas.online.dto.response;

public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        String ciudad,
        String rol) {
}
