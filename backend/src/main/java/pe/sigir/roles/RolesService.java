package pe.sigir.roles;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import pe.sigir.auditoria.Auditor;
import pe.sigir.compartido.NegocioException;
import pe.sigir.compartido.PeticionInvalidaException;
import pe.sigir.compartido.UsuarioActual;
import pe.sigir.usuarios.Rol;

import java.util.Set;
import java.util.TreeSet;

@ApplicationScoped
public class RolesService {

    private final RolPermisoDao dao;
    private final MatrizPermisos matriz;
    private final Auditor auditor;
    private final UsuarioActual actual;

    public RolesService(RolPermisoDao dao, MatrizPermisos matriz, Auditor auditor, UsuarioActual actual) {
        this.dao = dao;
        this.matriz = matriz;
        this.auditor = auditor;
        this.actual = actual;
    }

    @Transactional
    public Set<String> actualizar(Rol rol, Set<String> permisos) {
        for (String p : permisos) {
            if (!Permiso.CATALOGO.contains(p)) {
                throw new PeticionInvalidaException("Permiso desconocido: " + p);
            }
        }
        if (rol == Rol.ADMIN && !permisos.contains(Permiso.ROLES_GESTIONAR)) {
            throw new NegocioException("El rol ADMIN no puede perder la gestión de roles");
        }
        Set<String> antes = matriz.permisosDe(rol);
        dao.reemplazar(rol, permisos);
        matriz.recargar();
        auditor.registrar(actual.email(), "PERMISOS_ACTUALIZADOS", "ROL", rol.name(),
                "antes=" + new TreeSet<>(antes) + " despues=" + new TreeSet<>(permisos));
        return matriz.permisosDe(rol);
    }
}
