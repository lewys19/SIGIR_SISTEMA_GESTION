package pe.sigir.roles;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import pe.sigir.usuarios.Rol;

import java.util.HashSet;
import java.util.Set;

/**
 * El JWT solo trae el rol (ADMIN, ANALISTA…). Aquí se le agregan los permisos actuales de ese rol,
 * así un cambio en la matriz se aplica en la siguiente petición sin volver a iniciar sesión.
 */
@ApplicationScoped
public class PermisosAugmentor implements SecurityIdentityAugmentor {

    private final MatrizPermisos matriz;

    public PermisosAugmentor(MatrizPermisos matriz) {
        this.matriz = matriz;
    }

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identidad, AuthenticationRequestContext contexto) {
        if (identidad.isAnonymous()) {
            return Uni.createFrom().item(identidad);
        }
        Set<String> permisos = new HashSet<>();
        for (String nombre : identidad.getRoles()) {
            try {
                permisos.addAll(matriz.permisosDe(Rol.valueOf(nombre)));
            } catch (IllegalArgumentException ignorado) {
                // rol desconocido en el token: no aporta permisos
            }
        }
        return Uni.createFrom().item(QuarkusSecurityIdentity.builder(identidad).addRoles(permisos).build());
    }
}
