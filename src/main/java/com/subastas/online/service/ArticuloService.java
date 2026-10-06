package com.subastas.online.service;

import com.subastas.online.domain.Articulo;
import com.subastas.online.domain.ArticuloColeccionable;
import com.subastas.online.domain.ArticuloElectronico;
import com.subastas.online.domain.Usuario;
import com.subastas.online.dto.request.CrearArticuloColeccionableRequest;
import com.subastas.online.dto.request.CrearArticuloElectronicoRequest;
import com.subastas.online.dto.response.ArticuloResponse;
import com.subastas.online.exception.RecursoNoEncontradoException;
import com.subastas.online.mapper.ArticuloMapper;
import com.subastas.online.repository.ArticuloRepository;
import com.subastas.online.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArticuloService {

    private final ArticuloRepository articuloRepository;
    private final UsuarioRepository usuarioRepository;
    private final ArticuloMapper articuloMapper;

    public ArticuloService(ArticuloRepository articuloRepository,
            UsuarioRepository usuarioRepository,
            ArticuloMapper articuloMapper) {
        this.articuloRepository = articuloRepository;
        this.usuarioRepository = usuarioRepository;
        this.articuloMapper = articuloMapper;
    }

    @Transactional
    public ArticuloResponse crearElectronico(CrearArticuloElectronicoRequest request) {
        Usuario dueno = buscarUsuario(request.duenoId());
        Articulo articulo = new ArticuloElectronico(request.nombre(), request.descripcion(), request.marca(),
                request.modelo(), request.estadoUso(), dueno);
        return articuloMapper.toResponse(articuloRepository.save(articulo));
    }

    @Transactional
    public ArticuloResponse crearColeccionable(CrearArticuloColeccionableRequest request) {
        Usuario dueno = buscarUsuario(request.duenoId());
        Articulo articulo = new ArticuloColeccionable(request.nombre(), request.descripcion(), request.autor(),
                request.anioOrigen(), dueno);
        return articuloMapper.toResponse(articuloRepository.save(articulo));
    }

    @Transactional(readOnly = true)
    public ArticuloResponse obtenerPorId(Long id) {
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Artículo no encontrado con ID: " + id));
        return articuloMapper.toResponse(articulo);
    }

    @Transactional(readOnly = true)
    public List<ArticuloResponse> listar(Long duenoId) {
        List<Articulo> articulos = (duenoId == null)
                ? articuloRepository.findAll()
                : articuloRepository.findByDuenoId(duenoId);
        return articuloMapper.toResponseList(articulos);
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));
    }
}
