package pe.sigir.dashboard;

import pe.sigir.incidentes.IncidenteDtos.IncidenteResumen;

import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record Kpis(long total, long abiertos, long enRevision, long criticosAbiertos, long vencidos) {
    }

    public record PuntoMes(String mes, long registrados, long cerrados) {
    }

    public record PorSeveridad(String severidad, long total) {
    }

    public record PorEstado(String estado, long total) {
    }

    public record Dashboard(Kpis kpis, List<PuntoMes> porMes, List<PorSeveridad> porSeveridad,
                            List<PorEstado> porEstado, List<IncidenteResumen> urgentes) {
    }
}
