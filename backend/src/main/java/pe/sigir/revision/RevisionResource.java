package pe.sigir.revision;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import pe.sigir.incidentes.IncidenteDtos.IncidenteDetalle;
import pe.sigir.incidentes.IncidenteDtos.IncidenteResumen;
import pe.sigir.roles.Permiso;

import java.util.List;

@Path("/api/revisiones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(Permiso.REVISION_APROBAR)
public class RevisionResource {

    public record Comentario(@Size(max = 2000) String comentario) {
    }

    private final RevisionService servicio;

    public RevisionResource(RevisionService servicio) {
        this.servicio = servicio;
    }

    @GET
    @Path("/pendientes")
    public List<IncidenteResumen> pendientes() {
        return servicio.pendientes();
    }

    @POST
    @Path("/{id}/aprobar")
    public IncidenteDetalle aprobar(@PathParam("id") Long id, @Valid Comentario c) {
        return servicio.aprobar(id, c == null ? null : c.comentario());
    }

    @POST
    @Path("/{id}/rechazar")
    public IncidenteDetalle rechazar(@PathParam("id") Long id, @Valid Comentario c) {
        return servicio.rechazar(id, c == null ? null : c.comentario());
    }

    @POST
    @Path("/{id}/cerrar")
    public IncidenteDetalle cerrar(@PathParam("id") Long id, @Valid Comentario c) {
        return servicio.cerrar(id, c == null ? null : c.comentario());
    }
}
