package pe.sigir.incidentes;

import java.time.LocalDate;

/** Cada severidad fija el plazo máximo para reportar al regulador desde la detección. */
public enum Severidad {
    BAJA(15), MEDIA(7), ALTA(3), CRITICA(1);

    private final int diasPlazo;

    Severidad(int diasPlazo) {
        this.diasPlazo = diasPlazo;
    }

    public LocalDate fechaLimite(LocalDate deteccion) {
        return deteccion.plusDays(diasPlazo);
    }
}
