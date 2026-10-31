package pe.sigir.auth;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pe.sigir.usuarios.Usuario;

import java.util.Set;

/** Firma el JWT con la clave privada RS256 de la propia app (smallrye-jwt). */
@ApplicationScoped
public class JwtTokenEmisor implements TokenEmisor {

    private final String emisor;
    private final long duracion;

    public JwtTokenEmisor(@ConfigProperty(name = "mp.jwt.verify.issuer") String emisor,
                          @ConfigProperty(name = "sigir.jwt.duracion-segundos") long duracion) {
        this.emisor = emisor;
        this.duracion = duracion;
    }

    @Override
    public String emitir(Usuario usuario) {
        return Jwt.issuer(emisor)
                .subject(String.valueOf(usuario.id))
                .upn(usuario.email)
                .groups(Set.of(usuario.rol.name()))
                .claim("nombre", usuario.nombre)
                .expiresIn(duracion)
                .sign();
    }

    @Override
    public long duracionSegundos() {
        return duracion;
    }
}
