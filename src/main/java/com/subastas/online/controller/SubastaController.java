package com.subastas.online.controller;

import com.subastas.online.dto.request.CrearPujaRequest;
import com.subastas.online.dto.request.CrearSubastaRequest;
import com.subastas.online.dto.response.ParticipanteResponse;
import com.subastas.online.dto.response.PujaResponse;
import com.subastas.online.dto.response.SubastaResponse;
import com.subastas.online.dto.response.SubastaResumenResponse;
import com.subastas.online.service.SubastaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/subastas")
public class SubastaController {

    private static final String HEADER_USUARIO = "X-Usuario-Id";

    private final SubastaService subastaService;

    public SubastaController(SubastaService subastaService) {
        this.subastaService = subastaService;
    }

    @PostMapping
    public ResponseEntity<SubastaResponse> crear(@Valid @RequestBody CrearSubastaRequest request,
            UriComponentsBuilder uriBuilder) {
        SubastaResponse creada = subastaService.crear(request);
        URI uri = uriBuilder.path("/api/subastas/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(uri).body(creada);
    }

    // Solo ADMINISTRADOR
    @GetMapping("/activas")
    public ResponseEntity<List<SubastaResumenResponse>> listarActivas(
            @RequestHeader(HEADER_USUARIO) Long usuarioId) {
        return ResponseEntity.ok(subastaService.listarActivas(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubastaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(subastaService.obtenerPorId(id));
    }

    @PostMapping("/{id}/pujas")
    public ResponseEntity<PujaResponse> ofertar(@PathVariable Long id,
            @Valid @RequestBody CrearPujaRequest request,
            UriComponentsBuilder uriBuilder) {
        PujaResponse puja = subastaService.ofertar(id, request);
        URI uri = uriBuilder.path("/api/subastas/{id}/pujas").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).body(puja);
    }

    @GetMapping("/{id}/pujas")
    public ResponseEntity<List<PujaResponse>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(subastaService.listarPujas(id));
    }

    // Solo el vendedor de la subasta o un ADMINISTRADOR
    @GetMapping("/{id}/participantes")
    public ResponseEntity<List<ParticipanteResponse>> participantes(@PathVariable Long id,
            @RequestHeader(HEADER_USUARIO) Long usuarioId) {
        return ResponseEntity.ok(subastaService.listarParticipantes(id, usuarioId));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<SubastaResponse> cancelar(@PathVariable Long id,
            @RequestHeader(HEADER_USUARIO) Long usuarioId) {
        return ResponseEntity.ok(subastaService.cancelar(id, usuarioId));
    }

    // Solo ADMINISTRADOR (cierre manual para demostraciones)
    @PostMapping("/{id}/cerrar")
    public ResponseEntity<SubastaResponse> cerrar(@PathVariable Long id,
            @RequestHeader(HEADER_USUARIO) Long usuarioId) {
        return ResponseEntity.ok(subastaService.cerrarManualmente(id, usuarioId));
    }
}
