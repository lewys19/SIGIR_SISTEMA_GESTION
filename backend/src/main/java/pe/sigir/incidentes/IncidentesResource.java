package pe.sigir.incidentes;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;
import pe.sigir.compartido.Pagina;
import pe.sigir.incidentes.IncidenteDtos.*;
import pe.sigir.roles.Permiso;

@Path("/api/incidentes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IncidentesResource {

    private final IncidenteService servicio;

    public IncidentesResource(IncidenteService servicio) {
        this.servicio = servicio;
    }

    @GET
    @RolesAllowed(Permiso.INCIDENTES_VER)
    public Pagina<IncidenteResumen> buscar(@QueryParam("estado") EstadoIncidente estado,
                                           @QueryParam("severidad") Severidad severidad,
                                           @QueryParam("tipo") TipoIncidente tipo,
                                           @QueryParam("q") String texto,
                                           @QueryParam("pagina") @DefaultValue("0") int pagina,
                                           @QueryParam("tamano") @DefaultValue("20") int tamano) {
        return servicio.buscar(new FiltroIncidentes(estado, severidad, tipo, texto,
                Math.max(pagina, 0), Math.clamp(tamano, 1, 100)));
    }

    @GET
    @Path("/catalogos")
    @RolesAllowed(Permiso.INCIDENTES_VER)
    public Catalogos catalogos() {
        return new Catalogos(TipoIncidente.values(), Severidad.values(), Regulador.values(), EstadoIncidente.values());
    }

    @GET
    @Path("/{id}")
    @RolesAllowed(Permiso.INCIDENTES_VER)
    public IncidenteDetalle detalle(@PathParam("id") Long id) {
        return servicio.detalle(id);
    }

    @POST
    @ResponseStatus(201)
    @RolesAllowed(Permiso.INCIDENTES_CREAR)
    public IncidenteDetalle crear(@Valid IncidenteRequest peticion) {
        return servicio.crear(peticion);
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed(Permiso.INCIDENTES_EDITAR)
    public IncidenteDetalle actualizar(@PathParam("id") Long id, @Valid IncidenteRequest peticion) {
        return servicio.actualizar(id, peticion);
    }

    @POST
    @Path("/{id}/enviar")
    @RolesAllowed(Permiso.INCIDENTES_EDITAR)
    public IncidenteDetalle enviar(@PathParam("id") Long id) {
        return servicio.enviar(id);
    }
}
