package pe.sigir.roles;

import pe.sigir.usuarios.Rol;

import java.util.Map;
import java.util.Set;

public interface RolPermisoDao {
    Map<Rol, Set<String>> cargarMatriz();

    void reemplazar(Rol rol, Set<String> permisos);
}
