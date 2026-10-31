package pe.sigir.reportes;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pe.sigir.auditoria.Auditor;
import pe.sigir.compartido.PeticionInvalidaException;
import pe.sigir.compartido.UsuarioActual;
import pe.sigir.reportes.ReporteDao.Rango;
import pe.sigir.reportes.ReporteDtos.Resumen;
import pe.sigir.roles.Permiso;

import java.time.LocalDate;

@Path("/api/reportes")
@RolesAllowed(Permiso.REPORTES_VER)
public class ReportesResource {

    private final ReporteDao dao;
    private final Auditor auditor;
    private final UsuarioActual actual;

    public ReportesResource(ReporteDao dao, Auditor auditor, UsuarioActual actual) {
        this.dao = dao;
        this.auditor = auditor;
        this.actual = actual;
    }

    @GET
    @Path("/resumen")
    @Produces(MediaType.APPLICATION_JSON)
    public Resumen resumen(@QueryParam("desde") LocalDate desde, @QueryParam("hasta") LocalDate hasta) {
        Rango r = rango(desde, hasta);
        return new Resumen(dao.total(r), dao.porRegulador(r, LocalDate.now()), dao.porTipo(r), dao.cumplimiento(r));
    }

    @GET
    @Path("/incidentes.csv")
    @Produces("text/csv; charset=UTF-8")
    public Response csv(@QueryParam("desde") LocalDate desde, @QueryParam("hasta") LocalDate hasta) {
        Rango r = rango(desde, hasta);
        var incidentes = dao.incidentes(r);
        auditor.registrar(actual.email(), "REPORTE_EXPORTADO", "REPORTE", null,
                "incidentes.csv " + r.desde() + " a " + r.hasta() + " (" + incidentes.size() + " filas)");
        String nombre = "incidentes-" + r.desde() + "-a-" + r.hasta() + ".csv";
        return Response.ok(CsvIncidentes.generar(incidentes, LocalDate.now()))
                .header("Content-Disposition", "attachment; filename=\"" + nombre + "\"")
                .build();
    }

    @GET
    @Path("/incidentes.xlsx")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response excel(@QueryParam("desde") LocalDate desde, @QueryParam("hasta") LocalDate hasta) {
        Rango r = rango(desde, hasta);
        LocalDate hoy = LocalDate.now();
        var incidentes = dao.incidentes(r);
        Resumen resumen = new Resumen(dao.total(r), dao.porRegulador(r, hoy), dao.porTipo(r), dao.cumplimiento(r));
        byte[] archivo = ExcelIncidentes.generar(incidentes, resumen, r.desde(), r.hasta(), hoy);
        auditor.registrar(actual.email(), "REPORTE_EXPORTADO", "REPORTE", null,
                "incidentes.xlsx " + r.desde() + " a " + r.hasta() + " (" + incidentes.size() + " filas)");
        String nombre = "incidentes-" + r.desde() + "-a-" + r.hasta() + ".xlsx";
        return Response.ok(archivo)
                .header("Content-Disposition", "attachment; filename=\"" + nombre + "\"")
                .build();
    }

    /** Sin fechas: los últimos 90 días. */
    private static Rango rango(LocalDate desde, LocalDate hasta) {
        LocalDate h = hasta != null ? hasta : LocalDate.now();
        LocalDate d = desde != null ? desde : h.minusDays(90);
        if (d.isAfter(h)) {
            throw new PeticionInvalidaException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        return new Rango(d, h);
    }
}
