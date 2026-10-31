package pe.sigir.incidentes;

public record FiltroIncidentes(EstadoIncidente estado, Severidad severidad, TipoIncidente tipo, String texto,
                               int pagina, int tamano) {
}
