package pe.sigir.usuarios;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;
import pe.sigir.roles.Permiso;
import pe.sigir.usuarios.UsuarioDtos.*;

import java.util.List;

@Path("/api/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(Permiso.USUARIOS_GESTIONAR)
public class UsuariosResource {

    private final UsuarioService servicio;

    public UsuariosResource(UsuarioService servicio) {
        this.servicio = servicio;
    }

    @GET
    public List<UsuarioResponse> listar() {
        return servicio.listar().stream().map(UsuarioResponse::de).toList();
    }

    @POST
    @ResponseStatus(201)
    public UsuarioResponse crear(@Valid CrearUsuario peticion) {
        return UsuarioResponse.de(servicio.crear(peticion));
    }

    @PUT
    @Path("/{id}")
    public UsuarioResponse actualizar(@PathParam("id") Long id, @Valid ActualizarUsuario peticion) {
        return UsuarioResponse.de(servicio.actualizar(id, peticion));
    }

    @PUT
    @Path("/{id}/password")
    public void resetearPassword(@PathParam("id") Long id, @Valid NuevaPassword peticion) {
        servicio.resetearPassword(id, peticion.password());
    }
}
