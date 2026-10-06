package com.subastas.online.service;

import com.subastas.online.domain.Articulo;
import com.subastas.online.domain.EstadoSubasta;
import com.subastas.online.domain.RangoFechas;
import com.subastas.online.domain.Puja;
import com.subastas.online.domain.Subasta;
import com.subastas.online.domain.Usuario;
import com.subastas.online.dto.request.CrearPujaRequest;
import com.subastas.online.dto.request.CrearSubastaRequest;
import com.subastas.online.dto.response.ParticipanteResponse;
import com.subastas.online.dto.response.PujaResponse;
import com.subastas.online.dto.response.SubastaResponse;
import com.subastas.online.dto.response.SubastaResumenResponse;
import com.subastas.online.exception.AccesoDenegadoException;
import com.subastas.online.exception.ConflictoException;
import com.subastas.online.exception.RecursoNoEncontradoException;
import com.subastas.online.mapper.PujaMapper;
import com.subastas.online.mapper.SubastaMapper;
import com.subastas.online.repository.ArticuloRepository;
import com.subastas.online.repository.PujaRepository;
import com.subastas.online.repository.SubastaRepository;
import com.subastas.online.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubastaService {

    private final SubastaRepository subastaRepository;
    private final ArticuloRepository articuloRepository;
    private final UsuarioRepository usuarioRepository;
    private final PujaRepository pujaRepository;
    private final SubastaMapper subastaMapper;
    private final PujaMapper pujaMapper;

    public SubastaService(SubastaRepository subastaRepository,
            ArticuloRepository articuloRepository,
            UsuarioRepository usuarioRepository,
            PujaRepository pujaRepository,
            SubastaMapper subastaMapper,
            PujaMapper pujaMapper) {
        this.subastaRepository = subastaRepository;
        this.articuloRepository = articuloRepository;
        this.usuarioRepository = usuarioRepository;
        this.pujaRepository = pujaRepository;
        this.subastaMapper = subastaMapper;
        this.pujaMapper = pujaMapper;
    }

    @Transactional
    public SubastaResponse crear(CrearSubastaRequest request) {
        Usuario vendedor = buscarUsuario(request.vendedorId());
        Articulo articulo = articuloRepository.findById(request.articuloId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Artículo no encontrado con ID: " + request.articuloId()));

        // Regla 4: un artículo no puede tener más de una subasta PROGRAMADA o ACTIVA
        if (subastaRepository.existsByArticuloIdAndEstadoIn(articulo.getId(),
                List.of(EstadoSubasta.PROGRAMADA, EstadoSubasta.ACTIVA))) {
            throw new ConflictoException("El artículo ya tiene una subasta programada o activa");
        }

        RangoFechas periodo = new RangoFechas(request.inicio(), request.fin());

        // Invocación a las invariantes del modelo de dominio rico
        Subasta subasta = new Subasta(articulo, vendedor, request.precioBase(), request.incrementoMinimo(), periodo);
        return subastaMapper.toResponse(subastaRepository.save(subasta));
    }

    @Transactional(readOnly = true)
    public SubastaResponse obtenerPorId(Long id) {
        return subastaMapper.toResponse(buscarSubasta(id));
    }

    /** Solo administrador: subastas que están ocurriendo (estado ACTIVA). */
    @Transactional(readOnly = true)
    public List<SubastaResumenResponse> listarActivas(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        exigirAdministrador(usuario);
        return subastaMapper.toResumenList(subastaRepository.findByEstado(EstadoSubasta.ACTIVA));
    }

    @Transactional
    public PujaResponse ofertar(Long subastaId, CrearPujaRequest request) {
        Usuario postor = buscarUsuario(request.postorId());
        Subasta subasta = buscarSubastaConBloqueo(subastaId);

        Puja puja = subasta.ofertar(postor, request.monto());

        // La puja se guarda por cascada; el flush asigna su id antes de mapear la respuesta.
        subastaRepository.flush();
        return pujaMapper.toResponse(puja);
    }

    @Transactional(readOnly = true)
    public List<PujaResponse> listarPujas(Long subastaId) {
        if (!subastaRepository.existsById(subastaId)) {
            throw new RecursoNoEncontradoException("Subasta no encontrada con ID: " + subastaId);
        }
        return pujaMapper.toResponseList(pujaRepository.findBySubastaIdOrderByIdAsc(subastaId));
    }

    /** Solo el vendedor de la subasta o un administrador. */
    @Transactional(readOnly = true)
    public List<ParticipanteResponse> listarParticipantes(Long subastaId, Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        Subasta subasta = buscarSubasta(subastaId);
        if (!usuario.esAdministrador() && !subasta.esVendedor(usuario)) {
            throw new AccesoDenegadoException(
                    "Solo el vendedor de la subasta o un administrador puede ver los participantes");
        }
        return pujaMapper.toParticipanteList(subasta.resumenParticipantes());
    }

    @Transactional
    public SubastaResponse cancelar(Long subastaId, Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        Subasta subasta = buscarSubastaConBloqueo(subastaId);
        if (!usuario.esAdministrador() && !subasta.esVendedor(usuario)) {
            throw new AccesoDenegadoException("Solo el vendedor o un administrador puede cancelar la subasta");
        }
        subasta.cancelar(usuario.esAdministrador());
        return subastaMapper.toResponse(subastaRepository.save(subasta));
    }

    /** Cierre manual (demostraciones): solo administrador. */
    @Transactional
    public SubastaResponse cerrarManualmente(Long subastaId, Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        exigirAdministrador(usuario);
        Subasta subasta = buscarSubastaConBloqueo(subastaId);
        subasta.cerrar();
        return subastaMapper.toResponse(subastaRepository.save(subasta));
    }

    // ---- Usados por las tareas programadas (una transacción por subasta) ----

    @Transactional
    public void activarProgramada(Long subastaId) {
        Subasta subasta = buscarSubastaConBloqueo(subastaId);
        if (subasta.getEstado() == EstadoSubasta.PROGRAMADA) {
            subasta.activar();
            subastaRepository.save(subasta);
        }
    }

    @Transactional
    public void cerrarVencida(Long subastaId) {
        Subasta subasta = buscarSubastaConBloqueo(subastaId);
        if (subasta.getEstado() == EstadoSubasta.ACTIVA) {
            subasta.cerrar();
            subastaRepository.save(subasta);
        }
    }

    // ---- Helpers ----

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));
    }

    private Subasta buscarSubasta(Long id) {
        return subastaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada con ID: " + id));
    }

    private Subasta buscarSubastaConBloqueo(Long id) {
        return subastaRepository.findByIdConBloqueo(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada con ID: " + id));
    }

    private void exigirAdministrador(Usuario usuario) {
        if (!usuario.esAdministrador()) {
            throw new AccesoDenegadoException("Esta operación es solo para administradores");
        }
    }
}
