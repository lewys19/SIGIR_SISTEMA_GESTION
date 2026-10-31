package pe.sigir.reportes;

import java.util.List;

public final class ReporteDtos {

    private ReporteDtos() {
    }

    public record PorRegulador(String regulador, long total, long aprobados, long vencidos) {
    }

    public record PorTipo(String tipo, long total) {
    }

    public record Cumplimiento(long aTiempo, long fueraDePlazo, double porcentajeATiempo, Double promedioDiasAprobacion) {
    }

    public record Resumen(long total, List<PorRegulador> porRegulador, List<PorTipo> porTipo,
                          Cumplimiento cumplimiento) {
    }
}
