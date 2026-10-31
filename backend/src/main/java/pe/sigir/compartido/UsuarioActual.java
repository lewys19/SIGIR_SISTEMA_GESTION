package pe.sigir.compartido;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

/** Quién hace la petición actual, leído del JWT. */
@RequestScoped
public class UsuarioActual {

    @Inject
    JsonWebToken jwt;

    public Long id() {
        return Long.valueOf(jwt.getSubject());
    }

    public String email() {
        return jwt.getName();
    }
}
