package pe.sigir.compartido;

import org.apache.commons.lang3.StringUtils;

/** Normalización de textos con Apache Commons Lang. */
public final class Textos {

    private Textos() {
    }

    /** "  Fuga   de\tdatos " → "Fuga de datos"; vacío o solo espacios → null. */
    public static String limpiar(String texto) {
        return StringUtils.trimToNull(StringUtils.normalizeSpace(texto));
    }

    /** Quita espacios al inicio y final, pero conserva los saltos de línea internos (textos largos). */
    public static String recortar(String texto) {
        return StringUtils.trimToNull(texto);
    }

    /** Correo en minúsculas y sin espacios. */
    public static String email(String texto) {
        return StringUtils.lowerCase(StringUtils.trim(texto));
    }

    /** Corta textos largos con "…" (por ejemplo, el detalle de auditoría). */
    public static String resumir(String texto, int maximo) {
        return StringUtils.abbreviate(texto, "…", maximo);
    }
}
