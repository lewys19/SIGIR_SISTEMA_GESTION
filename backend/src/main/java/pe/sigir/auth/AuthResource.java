package pe.sigir.auth;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import pe.sigir.roles.Permiso;
import pe.sigir.usuarios.UsuarioService;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** Controlador REST: traduce HTTP a llamadas al servicio, sin lógica de negocio. */
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    public record CambiarPassword(@NotBlank String actual, @NotBlank String nueva) {
    }

    private final AuthService servicio;
    private final UsuarioService usuarios;
    private final JsonWebToken jwt;
    private final SecurityIdentity identidad;

    public AuthResource(AuthService servicio, UsuarioService usuarios, JsonWebToken jwt, SecurityIdentity identidad) {
        this.servicio = servicio;
        this.usuarios = usuarios;
        this.jwt = jwt;
        this.identidad = identidad;
    }

    @POST
    @Path("/login")
    public TokenResponse login(@Valid LoginRequest peticion) {
        return servicio.login(peticion.email(), peticion.password());
    }

    @GET
    @Path("/me")
    @Authenticated
    public UsuarioActualResponse me() {
        String rol = jwt.getGroups().stream().findFirst().orElse("");
        Set<String> permisos = identidad.getRoles().stream()
                .filter(Permiso.CATALOGO::contains)
                .collect(Collectors.toCollection(TreeSet::new));
        return new UsuarioActualResponse(jwt.getSubject(), jwt.getName(), jwt.getClaim("nombre"), rol, permisos);
    }

    @PUT
    @Path("/password")
    @Authenticated
    public void cambiarPassword(@Valid CambiarPassword peticion) {
        usuarios.cambiarMiPassword(peticion.actual(), peticion.nueva());
    }
}
