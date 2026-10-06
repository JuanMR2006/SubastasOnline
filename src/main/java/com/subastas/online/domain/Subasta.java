package com.subastas.online.domain;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "subastas")
public class Subasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ganador_id")
    private Usuario ganador;

    @Column(name = "precio_base", nullable = false)
    private double precioBase;

    @Column(name = "incremento_minimo", nullable = false)
    private double incrementoMinimo;

    @Embedded
    private RangoFechas periodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSubasta estado;

    @Column(name = "precio_final", nullable = false)
    private double precioFinal;

    @OneToMany(mappedBy = "subasta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Puja> pujas = new ArrayList<>();

    protected Subasta() {
    }

    public Subasta(Articulo articulo, Usuario vendedor, double precioBase, double incrementoMinimo,
            RangoFechas periodo) {
        if (articulo == null)
            throw new IllegalArgumentException("Artículo obligatorio");
        if (vendedor == null)
            throw new IllegalArgumentException("Vendedor obligatorio");
        if (periodo == null)
            throw new IllegalArgumentException("Periodo obligatorio");
        if (precioBase <= 0)
            throw new IllegalArgumentException("El precio base debe ser positivo");
        if (incrementoMinimo <= 0)
            throw new IllegalArgumentException("El incremento mínimo debe ser positivo");
        // Regla 4: el dueño del artículo debe ser el vendedor y el artículo estar DISPONIBLE
        if (!vendedor.equals(articulo.getDueno()))
            throw new IllegalArgumentException("El vendedor debe ser el dueño del artículo");
        if (articulo.getEstado() != EstadoArticulo.DISPONIBLE)
            throw new IllegalStateException("El artículo debe estar DISPONIBLE para ser subastado");

        this.articulo = articulo;
        this.vendedor = vendedor;
        this.precioBase = precioBase;
        this.incrementoMinimo = incrementoMinimo;
        this.periodo = periodo;
        this.estado = EstadoSubasta.PROGRAMADA;
        this.precioFinal = 0;
    }

    // Regla 5
    public void activar() {
        if (this.estado != EstadoSubasta.PROGRAMADA) {
            throw new IllegalStateException("Solo se puede activar una subasta PROGRAMADA");
        }
        this.articulo.cambiarEstado(EstadoArticulo.EN_SUBASTA);
        this.estado = EstadoSubasta.ACTIVA;
    }

    public Puja ofertar(Usuario postor, double monto) {
        return ofertar(postor, monto, LocalDateTime.now());
    }

    /** Variante con "ahora" explícito (útil para pruebas). */
    public Puja ofertar(Usuario postor, double monto, LocalDateTime ahora) {
        if (postor == null)
            throw new IllegalArgumentException("El postor es obligatorio");
        // Regla 1
        if (this.estado != EstadoSubasta.ACTIVA)
            throw new IllegalStateException("La subasta no está activa");
        if (!this.periodo.contiene(ahora))
            throw new IllegalStateException("La subasta está fuera de su periodo de vigencia");
        // Regla 3
        if (postor.equals(this.vendedor))
            throw new IllegalStateException("El vendedor no puede ofertar por su propio artículo");
        // Regla 2
        double minimo = ofertaMinimaSiguiente();
        if (monto < minimo)
            throw new IllegalArgumentException(
                    String.format("La oferta debe ser al menos %.2f", minimo));

        Puja puja = new Puja(this, postor, monto, ahora);
        this.pujas.add(puja);
        return puja;
    }

    /** Monto de la puja más alta; 0 si todavía no hay pujas. */
    public double ofertaActual() {
        return pujas.stream().mapToDouble(Puja::getMonto).max().orElse(0.0);
    }

    /** Mínimo que debe superar la próxima oferta (precio base si no hay pujas). */
    public double ofertaMinimaSiguiente() {
        return pujas.isEmpty() ? precioBase : ofertaActual() + incrementoMinimo;
    }

    /** Postores distintos que han ofertado. */
    public List<Usuario> participantes() {
        return pujas.stream().map(Puja::getPostor).distinct().toList();
    }

    /** Por postor: mejor oferta, cantidad de pujas y última puja; ordenado por mejor oferta descendente. */
    public List<ParticipacionPostor> resumenParticipantes() {
        Map<Usuario, List<Puja>> porPostor = pujas.stream()
                .collect(Collectors.groupingBy(Puja::getPostor, LinkedHashMap::new, Collectors.toList()));

        return porPostor.entrySet().stream()
                .map(e -> new ParticipacionPostor(
                        e.getKey(),
                        e.getValue().stream().mapToDouble(Puja::getMonto).max().orElse(0.0),
                        e.getValue().size(),
                        e.getValue().stream().map(Puja::getFechaHora).max(Comparator.naturalOrder()).orElse(null)))
                .sorted(Comparator.comparingDouble(ParticipacionPostor::mejorOferta).reversed())
                .toList();
    }

    // Regla 6
    public void cerrar() {
        if (this.estado != EstadoSubasta.ACTIVA) {
            throw new IllegalStateException("Solo se puede cerrar una subasta ACTIVA");
        }
        if (pujas.isEmpty()) {
            this.estado = EstadoSubasta.DESIERTA;
            this.articulo.cambiarEstado(EstadoArticulo.DISPONIBLE);
            return;
        }
        Puja ganadora = pujas.stream().max(Comparator.comparingDouble(Puja::getMonto)).orElseThrow();
        this.ganador = ganadora.getPostor();
        this.precioFinal = ganadora.getMonto();
        this.estado = EstadoSubasta.FINALIZADA;
        this.articulo.cambiarEstado(EstadoArticulo.VENDIDO);
    }

    // Regla 7
    public void cancelar(boolean esAdmin) {
        if (this.estado != EstadoSubasta.PROGRAMADA && this.estado != EstadoSubasta.ACTIVA) {
            throw new IllegalStateException("Solo se puede cancelar una subasta PROGRAMADA o ACTIVA");
        }
        if (!esAdmin && !pujas.isEmpty()) {
            throw new IllegalStateException("El vendedor no puede cancelar una subasta que ya tiene pujas");
        }
        if (this.articulo.getEstado() == EstadoArticulo.EN_SUBASTA) {
            this.articulo.cambiarEstado(EstadoArticulo.DISPONIBLE);
        }
        this.estado = EstadoSubasta.CANCELADA;
    }

    /** = articulo.calcularComision(precioFinal): polimórfico según el tipo de artículo. */
    public double comision() {
        return articulo.calcularComision(precioFinal);
    }

    /** Tiempo que falta para el cierre (cero si ya no está PROGRAMADA ni ACTIVA). */
    public Duration tiempoRestante(LocalDateTime ahora) {
        if (estado != EstadoSubasta.PROGRAMADA && estado != EstadoSubasta.ACTIVA) {
            return Duration.ZERO;
        }
        Duration restante = Duration.between(ahora, periodo.fin());
        return restante.isNegative() ? Duration.ZERO : restante;
    }

    public boolean esVendedor(Usuario usuario) {
        return this.vendedor.equals(usuario);
    }

    public Long getId() {
        return id;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public Usuario getVendedor() {
        return vendedor;
    }

    public Usuario getGanador() {
        return ganador;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    public double getIncrementoMinimo() {
        return incrementoMinimo;
    }

    public RangoFechas getPeriodo() {
        return periodo;
    }

    public EstadoSubasta getEstado() {
        return estado;
    }

    public double getPrecioFinal() {
        return precioFinal;
    }

    public List<Puja> getPujas() {
        return Collections.unmodifiableList(pujas);
    }
}
