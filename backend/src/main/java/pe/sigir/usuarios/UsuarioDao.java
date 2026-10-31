package pe.sigir.usuarios;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** DAO: lo único que el resto del sistema sabe sobre cómo se guardan los usuarios. */
public interface UsuarioDao {
    Optional<Usuario> buscarPorEmail(String email);

    default Optional<Usuario> buscarPorId(Long id) {
        throw new UnsupportedOperationException();
    }

    default List<Usuario> listarTodos() {
        throw new UnsupportedOperationException();
    }

    default List<Usuario> activosConRol(Collection<Rol> roles) {
        throw new UnsupportedOperationException();
    }

    default void guardar(Usuario usuario) {
        throw new UnsupportedOperationException();
    }
}
