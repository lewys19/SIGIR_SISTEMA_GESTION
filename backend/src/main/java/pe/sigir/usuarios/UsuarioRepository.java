package pe.sigir.usuarios;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Implementación del DAO con Panache en modo Repository. */
@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario>, UsuarioDao {

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return findByIdOptional(id);
    }

    @Override
    public List<Usuario> listarTodos() {
        return listAll(Sort.ascending("nombre"));
    }

    @Override
    public List<Usuario> activosConRol(Collection<Rol> roles) {
        if (roles.isEmpty()) {
            return List.of();
        }
        return list("activo = true and rol in ?1", roles);
    }

    @Override
    public void guardar(Usuario usuario) {
        persist(usuario);
    }
}
