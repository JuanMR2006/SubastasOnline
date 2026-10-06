package com.subastas.online.service;

import com.subastas.online.domain.Usuario;
import com.subastas.online.dto.request.CrearUsuarioRequest;
import com.subastas.online.dto.response.UsuarioResponse;
import com.subastas.online.exception.ConflictoException;
import com.subastas.online.exception.RecursoNoEncontradoException;
import com.subastas.online.mapper.UsuarioMapper;
import com.subastas.online.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioService(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest request) {
        if (usuarioRepository.existsByCorreo(request.correo())) {
            throw new ConflictoException("Ya existe un usuario con el correo: " + request.correo());
        }
        Usuario usuario = new Usuario(request.nombre(), request.correo(), request.ciudad(), request.rol());
        Usuario guardado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));
        return usuarioMapper.toResponse(usuario);
    }
}
