package com.subastas.online.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Polimorfismo real: Articulo.calcularComision() invoca this.porcentajeComision()
 * y el despacho dinámico elige la versión del tipo real de cada objeto.
 */
class ArticuloPolimorfismoTest {

    private Usuario juan;
    private Usuario ana;
    private Articulo celular;
    private Articulo moneda;

    @BeforeEach
    void preparar() {
        juan = new Usuario("Juan", "juan@mail.com", "Pasto", Rol.USUARIO);
        ana = new Usuario("Ana", "ana@mail.com", "Cali", Rol.USUARIO);
        // Mismos datos del diagrama de Stack/Heap
        celular = new ArticuloElectronico("Celular A54", "Como nuevo", "Samsung", "A54", "Usado", juan);
        moneda = new ArticuloColeccionable("Moneda 1910", "Plata", "Casa de Moneda", 1910, ana);
    }

    @Test
    void electronicoNoSobrescribeYUsaElPorcentajeDelPadre() {
        assertEquals(Articulo.COMISION_BASE, celular.porcentajeComision(), 1e-9);
        assertEquals(0.05, celular.porcentajeComision(), 1e-9);
        assertEquals(44_000.0, celular.calcularComision(880_000), 0.01);
    }

    @Test
    void coleccionableSobrescribePorcentajeYCambiaLaComision() {
        assertEquals(0.08, moneda.porcentajeComision(), 1e-9);
        assertEquals(96_000.0, moneda.calcularComision(1_200_000), 0.01);
    }

    @Test
    void mismaLlamadaEjecutaMetodosDistintosSegunElTipoReal() {
        List<Articulo> articulos = List.of(celular, moneda);
        double[] esperados = { 0.05, 0.08 };

        for (int i = 0; i < articulos.size(); i++) {
            Articulo a = articulos.get(i); // referencia de tipo Articulo
            System.out.printf("%-22s porcentaje=%.2f  comision(1.000.000)=%.0f  -> %s%n",
                    a.getClass().getSimpleName(), a.porcentajeComision(), a.calcularComision(1_000_000),
                    a.describir());
            assertEquals(esperados[i], a.porcentajeComision(), 1e-9);
            assertEquals(1_000_000 * esperados[i], a.calcularComision(1_000_000), 0.01);
        }
    }

    @Test
    void describirEsDistintoEnCadaSubtipo() {
        assertEquals("Celular A54 - Samsung A54 (Usado)", celular.describir());
        assertEquals("Moneda 1910 - Casa de Moneda (1910)", moneda.describir());
    }

    @Test
    void comisionNoAceptaPreciosNegativos() {
        assertThrows(IllegalArgumentException.class, () -> celular.calcularComision(-1));
    }

    @Test
    void articuloNuevoEstaDisponible() {
        assertEquals(EstadoArticulo.DISPONIBLE, celular.getEstado());
    }

    @Test
    void transicionesValidasDeEstado() {
        celular.cambiarEstado(EstadoArticulo.EN_SUBASTA);
        assertEquals(EstadoArticulo.EN_SUBASTA, celular.getEstado());
        celular.cambiarEstado(EstadoArticulo.DISPONIBLE);
        assertEquals(EstadoArticulo.DISPONIBLE, celular.getEstado());
        celular.cambiarEstado(EstadoArticulo.EN_SUBASTA);
        celular.cambiarEstado(EstadoArticulo.VENDIDO);
        assertEquals(EstadoArticulo.VENDIDO, celular.getEstado());
    }

    @Test
    void transicionesInvalidasDeEstado() {
        // DISPONIBLE -> VENDIDO no está permitido
        assertThrows(IllegalStateException.class, () -> celular.cambiarEstado(EstadoArticulo.VENDIDO));
        // DISPONIBLE -> DISPONIBLE tampoco
        assertThrows(IllegalStateException.class, () -> celular.cambiarEstado(EstadoArticulo.DISPONIBLE));
        // VENDIDO no cambia
        celular.cambiarEstado(EstadoArticulo.EN_SUBASTA);
        celular.cambiarEstado(EstadoArticulo.VENDIDO);
        assertThrows(IllegalStateException.class, () -> celular.cambiarEstado(EstadoArticulo.DISPONIBLE));
        assertThrows(IllegalStateException.class, () -> celular.cambiarEstado(EstadoArticulo.EN_SUBASTA));
    }
}
