package pe.sigir.usuarios;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import pe.sigir.auth.PasswordHasher;

/** Si la tabla está vacía, crea el primer administrador con los datos de configuración. */
@ApplicationScoped
public class AdminInicial {

    private static final Logger LOG = Logger.getLogger(AdminInicial.class);

    private final UsuarioRepository repositorio;
    private final PasswordHasher hasher;
    private final String email;
    private final String nombre;
    private final String password;

    public AdminInicial(UsuarioRepository repositorio, PasswordHasher hasher,
                        @ConfigProperty(name = "sigir.admin.email") String email,
                        @ConfigProperty(name = "sigir.admin.nombre") String nombre,
                        @ConfigProperty(name = "sigir.admin.password") String password) {
        this.repositorio = repositorio;
        this.hasher = hasher;
        this.email = email;
        this.nombre = nombre;
        this.password = password;
    }

    @Transactional
    void alArrancar(@Observes StartupEvent evento) {
        if (repositorio.count() > 0) {
            return;
        }
        Usuario admin = new Usuario();
        admin.email = email.trim().toLowerCase();
        admin.nombre = nombre;
        admin.passwordHash = hasher.hashear(password);
        admin.rol = Rol.ADMIN;
        repositorio.persist(admin);
        LOG.infof("Administrador inicial creado: %s", admin.email);
    }
}
