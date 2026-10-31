package pe.sigir.incidentes;

import org.junit.jupiter.api.Test;
import pe.sigir.compartido.NegocioException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Máquina de estados y plazos regulatorios: lógica de dominio pura. */
class IncidenteTest {

    private Incidente nuevo() {
        Incidente i = new Incidente();
        i.estado = EstadoIncidente.BORRADOR;
        return i;
    }

    @Test
    void flujoFelizBorradorRevisionAprobadoCerrado() {
        Incidente i = nuevo();
        i.enviarARevision();
        assertEquals(EstadoIncidente.EN_REVISION, i.estado);
        i.aprobar();
        assertEquals(EstadoIncidente.APROBADO, i.estado);
        i.cerrar();
        assertEquals(EstadoIncidente.CERRADO, i.estado);
    }

    @Test
    void rechazadoVuelveAEditarseYReenviarse() {
        Incidente i = nuevo();
        i.enviarARevision();
        i.rechazar();
        assertTrue(i.esEditable());
        i.enviarARevision();
        assertEquals(EstadoIncidente.EN_REVISION, i.estado);
    }

    @Test
    void noSePuedeAprobarUnBorrador() {
        assertThrows(NegocioException.class, () -> nuevo().aprobar());
    }

    @Test
    void noSePuedeCerrarSinAprobar() {
        Incidente i = nuevo();
        i.enviarARevision();
        assertThrows(NegocioException.class, i::cerrar);
    }

    @Test
    void enRevisionNoEsEditable() {
        Incidente i = nuevo();
        i.enviarARevision();
        assertFalse(i.esEditable());
    }

    @Test
    void cerradoEsFinal() {
        Incidente i = nuevo();
        i.enviarARevision();
        i.aprobar();
        i.cerrar();
        assertThrows(NegocioException.class, i::enviarARevision);
        assertThrows(NegocioException.class, i::rechazar);
    }

    @Test
    void plazoDeReporteSegunSeveridad() {
        LocalDate deteccion = LocalDate.of(2026, 10, 1);
        assertEquals(LocalDate.of(2026, 10, 2), Severidad.CRITICA.fechaLimite(deteccion));
        assertEquals(LocalDate.of(2026, 10, 4), Severidad.ALTA.fechaLimite(deteccion));
        assertEquals(LocalDate.of(2026, 10, 8), Severidad.MEDIA.fechaLimite(deteccion));
        assertEquals(LocalDate.of(2026, 10, 16), Severidad.BAJA.fechaLimite(deteccion));
    }
}
