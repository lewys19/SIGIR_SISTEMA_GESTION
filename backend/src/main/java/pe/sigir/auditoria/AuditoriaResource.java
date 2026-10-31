package pe.sigir.auditoria;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import pe.sigir.compartido.Pagina;
import pe.sigir.roles.Permiso;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Path("/api/auditoria")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(Permiso.AUDITORIA_VER)
public class AuditoriaResource {

    public record RegistroResponse(Long id, OffsetDateTime fecha, String actor, String accion,
                                   String entidad, String entidadId, String detalle) {
        static RegistroResponse de(RegistroAuditoria r) {
            return new RegistroResponse(r.id, r.fecha, r.actor, r.accion, r.entidad, r.entidadId, r.detalle);
        }
    }

    private final AuditoriaDao dao;

    public AuditoriaResource(AuditoriaDao dao) {
        this.dao = dao;
    }

    @GET
    public Pagina<RegistroResponse> buscar(@QueryParam("accion") String accion,
                                           @QueryParam("actor") String actor,
                                           @QueryParam("desde") LocalDate desde,
                                           @QueryParam("hasta") LocalDate hasta,
                                           @QueryParam("pagina") @DefaultValue("0") int pagina,
                                           @QueryParam("tamano") @DefaultValue("20") int tamano) {
        var filtro = new FiltroAuditoria(accion, actor, desde, hasta, Math.max(pagina, 0), Math.clamp(tamano, 1, 100));
        return dao.buscar(filtro).map(RegistroResponse::de);
    }

    @GET
    @Path("/acciones")
    public List<String> acciones() {
        return dao.acciones();
    }
}
