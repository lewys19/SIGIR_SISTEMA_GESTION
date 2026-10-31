package pe.sigir.compartido;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextosTest {

    @Test
    void limpiarUneEspaciosRepetidos() {
        assertEquals("Fuga de datos", Textos.limpiar("  Fuga   de\tdatos \n"));
        assertNull(Textos.limpiar("   "));
        assertNull(Textos.limpiar(null));
    }

    @Test
    void recortarConservaLosSaltosDeLinea() {
        assertEquals("Línea 1\nLínea 2", Textos.recortar("  Línea 1\nLínea 2  "));
    }

    @Test
    void emailEnMinusculas() {
        assertEquals("ana@sigir.pe", Textos.email("  ANA@Sigir.PE "));
    }

    @Test
    void resumirCortaConPuntosSuspensivos() {
        assertEquals("abcdefghi…", Textos.resumir("abcdefghijklmnop", 10));
        assertEquals("corto", Textos.resumir("corto", 10));
    }
}
