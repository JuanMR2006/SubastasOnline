package com.subastas.online.controller;

import com.subastas.online.dto.request.CrearArticuloColeccionableRequest;
import com.subastas.online.dto.request.CrearArticuloElectronicoRequest;
import com.subastas.online.dto.response.ArticuloResponse;
import com.subastas.online.service.ArticuloService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/articulos")
public class ArticuloController {

    private final ArticuloService articuloService;

    public ArticuloController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @PostMapping("/electronicos")
    public ResponseEntity<ArticuloResponse> crearElectronico(
            @Valid @RequestBody CrearArticuloElectronicoRequest request,
            UriComponentsBuilder uriBuilder) {
        ArticuloResponse creado = articuloService.crearElectronico(request);
        URI uri = uriBuilder.path("/api/articulos/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(uri).body(creado);
    }

    @PostMapping("/coleccionables")
    public ResponseEntity<ArticuloResponse> crearColeccionable(
            @Valid @RequestBody CrearArticuloColeccionableRequest request,
            UriComponentsBuilder uriBuilder) {
        ArticuloResponse creado = articuloService.crearColeccionable(request);
        URI uri = uriBuilder.path("/api/articulos/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(uri).body(creado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticuloResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(articuloService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<ArticuloResponse>> listar(@RequestParam(required = false) Long duenoId) {
        return ResponseEntity.ok(articuloService.listar(duenoId));
    }
}
