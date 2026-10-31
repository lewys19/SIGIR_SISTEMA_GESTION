package pe.sigir.auth;

import com.google.common.base.Ticker;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ProteccionFuerzaBrutaTest {

    /** Reloj manual: permite "adelantar" 15 minutos sin esperar. */
    static class RelojFalso extends Ticker {
        long nanos;

        @Override
        public long read() {
            return nanos;
        }

        void avanzar(long minutos) {
            nanos += TimeUnit.MINUTES.toNanos(minutos);
        }
    }

    private final RelojFalso reloj = new RelojFalso();
    private final ProteccionFuerzaBruta proteccion = new ProteccionFuerzaBruta(5, Duration.ofMinutes(15), reloj);

    @Test
    void bloqueaAlQuintoIntentoFallido() {
        for (int i = 0; i < 4; i++) {
            proteccion.registrarFallo("ana@sigir.pe");
        }
        assertFalse(proteccion.estaBloqueado("ana@sigir.pe"));
        proteccion.registrarFallo("ana@sigir.pe");
        assertTrue(proteccion.estaBloqueado("ana@sigir.pe"));
        assertFalse(proteccion.estaBloqueado("otro@sigir.pe"), "el bloqueo es por cuenta");
    }

    @Test
    void elBloqueoSeLevantaALos15Minutos() {
        for (int i = 0; i < 5; i++) {
            proteccion.registrarFallo("ana@sigir.pe");
        }
        reloj.avanzar(14);
        assertTrue(proteccion.estaBloqueado("ana@sigir.pe"));
        reloj.avanzar(2);
        assertFalse(proteccion.estaBloqueado("ana@sigir.pe"));
    }

    @Test
    void unLoginCorrectoReiniciaElContador() {
        for (int i = 0; i < 4; i++) {
            proteccion.registrarFallo("ana@sigir.pe");
        }
        proteccion.reiniciar("ana@sigir.pe");
        proteccion.registrarFallo("ana@sigir.pe");
        assertFalse(proteccion.estaBloqueado("ana@sigir.pe"));
    }
}
