package com.subastas.online.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas de negocio del dominio rico (sin Spring ni base de datos). */
class SubastaTest {

    private Usuario juan;   // vendedor
    private Usuario ana;    // postora
    private Usuario luis;   // postor
    private Usuario admin;
    private ArticuloElectronico celular;
    private ArticuloColeccionable moneda;

    @BeforeEach
    void preparar() {
        juan = new Usuario("Juan", "juan@mail.com", "Pasto", Rol.USUARIO);
        ana = new Usuario("Ana", "ana@mail.com", "Cali", Rol.USUARIO);
        luis = new Usuario("Luis", "luis@mail.com", "Bogotá", Rol.USUARIO);
        admin = new Usuario("Admin", "admin@mail.com", "Pasto", Rol.ADMINISTRADOR);
        celular = new ArticuloElectronico("Celular A54", "Como nuevo", "Samsung", "A54", "Usado", juan);
        moneda = new ArticuloColeccionable("Moneda 1910", "Plata", "Casa de Moneda", 1910, juan);
    }

    private Subasta programada(Articulo articulo, double base, double incremento) {
        LocalDateTime ahora = LocalDateTime.now();
        return new Subasta(articulo, juan, base, incremento,
                new RangoFechas(ahora.minusHours(1), ahora.plusHours(1)));
    }

    private Subasta activa(Articulo articulo, double base, double incremento) {
        Subasta s = programada(articulo, base, incremento);
        s.activar();
        return s;
    }

    // ---------- creación (regla 4) ----------

    @Test
    void subastaNuevaNaceProgramadaYElArticuloSigueDisponible() {
        Subasta s = programada(celular, 500_000, 50_000);
        assertEquals(EstadoSubasta.PROGRAMADA, s.getEstado());
        assertEquals(EstadoArticulo.DISPONIBLE, celular.getEstado());
    }

    @Test
    void elVendedorDebeSerElDuenoDelArticulo() {
        LocalDateTime ahora = LocalDateTime.now();
        RangoFechas periodo = new RangoFechas(ahora, ahora.plusHours(1));
        assertThrows(IllegalArgumentException.class,
                () -> new Subasta(celular, ana, 500_000, 50_000, periodo));
    }

    @Test
    void elArticuloDebeEstarDisponible() {
        celular.cambiarEstado(EstadoArticulo.EN_SUBASTA);
        assertThrows(IllegalStateException.class, () -> programada(celular, 500_000, 50_000));
    }

    @Test
    void precioBaseEIncrementoDebenSerPositivos() {
        assertThrows(IllegalArgumentException.class, () -> programada(celular, 0, 50_000));
        assertThrows(IllegalArgumentException.class, () -> programada(celular, 500_000, 0));
    }

    // ---------- activar (regla 5) ----------

    @Test
    void activarPasaLaSubastaAActivaYElArticuloAEnSubasta() {
        Subasta s = programada(celular, 500_000, 50_000);
        s.activar();
        assertEquals(EstadoSubasta.ACTIVA, s.getEstado());
        assertEquals(EstadoArticulo.EN_SUBASTA, celular.getEstado());
    }

    @Test
    void soloSePuedeActivarUnaSubastaProgramada() {
        Subasta s = activa(celular, 500_000, 50_000);
        assertThrows(IllegalStateException.class, s::activar);
    }

    // ---------- ofertar (reglas 1, 2 y 3) ----------

    @Test
    void noSePuedeOfertarSiLaSubastaNoEstaActiva() {
        Subasta s = programada(celular, 500_000, 50_000);
        assertThrows(IllegalStateException.class, () -> s.ofertar(ana, 500_000));
    }

    @Test
    void noSePuedeOfertarFueraDelPeriodo() {
        Subasta s = activa(celular, 500_000, 50_000);
        assertThrows(IllegalStateException.class,
                () -> s.ofertar(ana, 500_000, LocalDateTime.now().plusHours(2)));
        assertThrows(IllegalStateException.class,
                () -> s.ofertar(ana, 500_000, LocalDateTime.now().minusHours(2)));
    }

    @Test
    void elVendedorNoPuedeOfertarPorSuPropioArticulo() {
        Subasta s = activa(celular, 500_000, 50_000);
        assertThrows(IllegalStateException.class, () -> s.ofertar(juan, 600_000));
    }

    @Test
    void laPrimeraOfertaDebeSerAlMenosElPrecioBase() {
        Subasta s = activa(celular, 500_000, 50_000);
        assertThrows(IllegalArgumentException.class, () -> s.ofertar(ana, 499_999));
        Puja puja = s.ofertar(ana, 500_000);
        assertEquals(500_000, puja.getMonto(), 0.001);
        assertSame(ana, puja.getPostor());
    }

    @Test
    void lasSiguientesOfertasDebenSuperarLaActualMasElIncremento() {
        Subasta s = activa(celular, 500_000, 50_000);
        s.ofertar(ana, 500_000);
        assertEquals(550_000, s.ofertaMinimaSiguiente(), 0.001);
        assertThrows(IllegalArgumentException.class, () -> s.ofertar(luis, 549_999));
        s.ofertar(luis, 550_000);
        assertEquals(550_000, s.ofertaActual(), 0.001);
    }

    @Test
    void ofertaActualEsCeroSinPujasYElPrecioBaseEsElMinimo() {
        Subasta s = activa(celular, 500_000, 50_000);
        assertEquals(0.0, s.ofertaActual(), 0.001);
        assertEquals(500_000, s.ofertaMinimaSiguiente(), 0.001);
    }

    // ---------- participantes (requisito del profesor) ----------

    @Test
    void participantesSonLosPostoresDistintos() {
        Subasta s = activa(celular, 500_000, 50_000);
        s.ofertar(ana, 500_000);
        s.ofertar(luis, 550_000);
        s.ofertar(ana, 600_000);

        List<Usuario> participantes = s.participantes();
        assertEquals(2, participantes.size());
        assertTrue(participantes.contains(ana));
        assertTrue(participantes.contains(luis));
        assertFalse(participantes.contains(juan));
    }

    @Test
    void resumenDeParticipantesVieneOrdenadoPorMejorOfertaDescendente() {
        Subasta s = activa(celular, 500_000, 50_000);
        s.ofertar(ana, 500_000);
        s.ofertar(luis, 550_000);
        s.ofertar(ana, 600_000);

        List<ParticipacionPostor> resumen = s.resumenParticipantes();
        assertEquals(2, resumen.size());

        assertSame(ana, resumen.get(0).postor());
        assertEquals(600_000, resumen.get(0).mejorOferta(), 0.001);
        assertEquals(2, resumen.get(0).cantidadPujas());
        assertNotNull(resumen.get(0).fechaUltimaPuja());

        assertSame(luis, resumen.get(1).postor());
        assertEquals(550_000, resumen.get(1).mejorOferta(), 0.001);
        assertEquals(1, resumen.get(1).cantidadPujas());
    }

    @Test
    void sinPujasNoHayParticipantes() {
        Subasta s = activa(celular, 500_000, 50_000);
        assertTrue(s.participantes().isEmpty());
        assertTrue(s.resumenParticipantes().isEmpty());
    }

    // ---------- cerrar (regla 6) ----------

    @Test
    void cerrarSinPujasDejaLaSubastaDesiertaYLiberaElArticulo() {
        Subasta s = activa(celular, 500_000, 50_000);
        s.cerrar();
        assertEquals(EstadoSubasta.DESIERTA, s.getEstado());
        assertEquals(EstadoArticulo.DISPONIBLE, celular.getEstado());
        assertNull(s.getGanador());
    }

    @Test
    void cerrarConPujasAsignaGanadorPrecioFinalYVendeElArticulo() {
        Subasta s = activa(celular, 500_000, 50_000);
        s.ofertar(ana, 500_000);
        s.ofertar(luis, 600_000);
        s.ofertar(ana, 880_000);

        s.cerrar();

        assertEquals(EstadoSubasta.FINALIZADA, s.getEstado());
        assertSame(ana, s.getGanador());
        assertEquals(880_000, s.getPrecioFinal(), 0.001);
        assertEquals(EstadoArticulo.VENDIDO, celular.getEstado());
    }

    @Test
    void soloSePuedeCerrarUnaSubastaActiva() {
        Subasta s = programada(celular, 500_000, 50_000);
        assertThrows(IllegalStateException.class, s::cerrar);
    }

    // ---------- comisión polimórfica ----------

    @Test
    void comisionDelElectronicoEsElCincoPorCiento() {
        Subasta s = activa(celular, 500_000, 50_000);
        s.ofertar(ana, 880_000);
        s.cerrar();
        assertEquals(44_000, s.comision(), 0.01);
    }

    @Test
    void comisionDelColeccionableEsElOchoPorCiento() {
        Subasta s = activa(moneda, 1_000_000, 100_000);
        s.ofertar(ana, 1_200_000);
        s.cerrar();
        assertEquals(96_000, s.comision(), 0.01);
    }

    // ---------- cancelar (regla 7) ----------

    @Test
    void elVendedorPuedeCancelarSiNoHayPujasYElArticuloQuedaDisponible() {
        Subasta s = activa(celular, 500_000, 50_000);
        s.cancelar(false);
        assertEquals(EstadoSubasta.CANCELADA, s.getEstado());
        assertEquals(EstadoArticulo.DISPONIBLE, celular.getEstado());
    }

    @Test
    void elVendedorNoPuedeCancelarSiYaHayPujas() {
        Subasta s = activa(celular, 500_000, 50_000);
        s.ofertar(ana, 500_000);
        assertThrows(IllegalStateException.class, () -> s.cancelar(false));
        assertEquals(EstadoSubasta.ACTIVA, s.getEstado());
    }

    @Test
    void elAdministradorSiempreCancelaAunqueHayaPujas() {
        Subasta s = activa(celular, 500_000, 50_000);
        s.ofertar(ana, 500_000);
        assertTrue(admin.esAdministrador());
        s.cancelar(admin.esAdministrador());
        assertEquals(EstadoSubasta.CANCELADA, s.getEstado());
        assertEquals(EstadoArticulo.DISPONIBLE, celular.getEstado());
    }

    @Test
    void cancelarUnaSubastaProgramadaTambienFunciona() {
        Subasta s = programada(celular, 500_000, 50_000);
        s.cancelar(false);
        assertEquals(EstadoSubasta.CANCELADA, s.getEstado());
        assertEquals(EstadoArticulo.DISPONIBLE, celular.getEstado());
    }

    @Test
    void noSePuedeCancelarUnaSubastaYaCerrada() {
        Subasta s = activa(celular, 500_000, 50_000);
        s.cerrar();
        assertThrows(IllegalStateException.class, () -> s.cancelar(true));
    }

    // ---------- tiempo restante ----------

    @Test
    void tiempoRestanteEsPositivoSiEstaActivaYCeroSiYaCerro() {
        Subasta s = activa(celular, 500_000, 50_000);
        Duration restante = s.tiempoRestante(LocalDateTime.now());
        assertTrue(restante.toMinutes() >= 59);
        s.cerrar();
        assertEquals(Duration.ZERO, s.tiempoRestante(LocalDateTime.now()));
    }

    @Test
    void usuarioAdministradorSeIdentificaPorSuRol() {
        assertTrue(admin.esAdministrador());
        assertFalse(juan.esAdministrador());
    }
}
