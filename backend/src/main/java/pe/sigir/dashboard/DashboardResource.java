package pe.sigir.dashboard;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pe.sigir.dashboard.DashboardDtos.*;
import pe.sigir.incidentes.EstadoIncidente;
import pe.sigir.incidentes.IncidenteDtos.IncidenteResumen;
import pe.sigir.incidentes.Severidad;
import pe.sigir.roles.Permiso;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

@Path("/api/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(Permiso.DASHBOARD_VER)
public class DashboardResource {

    private static final int MESES = 6;

    private final DashboardDao dao;

    public DashboardResource(DashboardDao dao) {
        this.dao = dao;
    }

    @GET
    public Dashboard dashboard() {
        LocalDate hoy = LocalDate.now();
        Map<String, Long> sev = dao.conteoPor("severidad");
        Map<String, Long> est = dao.conteoPor("estado");
        return new Dashboard(
                dao.kpis(hoy),
                dao.porMes(hoy.withDayOfMonth(1).minusMonths(MESES - 1), MESES),
                Arrays.stream(Severidad.values()).map(s -> new PorSeveridad(s.name(), sev.getOrDefault(s.name(), 0L))).toList(),
                Arrays.stream(EstadoIncidente.values()).map(e -> new PorEstado(e.name(), est.getOrDefault(e.name(), 0L))).toList(),
                dao.urgentes(5).stream().map(IncidenteResumen::de).toList());
    }
}
