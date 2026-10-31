package pe.sigir.roles;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import pe.sigir.usuarios.Rol;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Path("/api/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(Permiso.ROLES_GESTIONAR)
public class RolesResource {

    public record RolPermisos(Rol rol, Set<String> permisos) {
    }

    public record MatrizResponse(List<String> catalogo, List<RolPermisos> roles) {
    }

    public record ActualizarPermisos(@NotNull Set<String> permisos) {
    }

    private final MatrizPermisos matriz;
    private final RolesService servicio;

    public RolesResource(MatrizPermisos matriz, RolesService servicio) {
        this.matriz = matriz;
        this.servicio = servicio;
    }

    @GET
    public MatrizResponse matriz() {
        List<RolPermisos> roles = Arrays.stream(Rol.values())
                .map(r -> new RolPermisos(r, new TreeSet<>(matriz.permisosDe(r))))
                .toList();
        return new MatrizResponse(Permiso.CATALOGO, roles);
    }

    @PUT
    @Path("/{rol}")
    public RolPermisos actualizar(@PathParam("rol") Rol rol, @Valid ActualizarPermisos peticion) {
        return new RolPermisos(rol, new TreeSet<>(servicio.actualizar(rol, peticion.permisos())));
    }
}
