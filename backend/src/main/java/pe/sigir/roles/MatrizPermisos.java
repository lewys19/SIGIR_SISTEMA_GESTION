package pe.sigir.roles;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import pe.sigir.usuarios.Rol;

import java.util.*;

/** Caché en memoria de la matriz rol → permisos (se consulta en cada petición). */
@ApplicationScoped
public class MatrizPermisos {

    private final RolPermisoDao dao;
    private volatile Map<Rol, Set<String>> matriz = Map.of();

    public MatrizPermisos(RolPermisoDao dao) {
        this.dao = dao;
    }

    @Transactional
    void alArrancar(@Observes StartupEvent e) {
        recargar();
    }

    public void recargar() {
        Map<Rol, Set<String>> nueva = new EnumMap<>(Rol.class);
        dao.cargarMatriz().forEach((rol, permisos) -> nueva.put(rol, Set.copyOf(permisos)));
        matriz = Collections.unmodifiableMap(nueva);
    }

    public Set<String> permisosDe(Rol rol) {
        return matriz.getOrDefault(rol, Set.of());
    }

    public Set<Rol> rolesCon(String permiso) {
        Set<Rol> roles = EnumSet.noneOf(Rol.class);
        matriz.forEach((rol, permisos) -> {
            if (permisos.contains(permiso)) {
                roles.add(rol);
            }
        });
        return roles;
    }

    public Map<Rol, Set<String>> todo() {
        return matriz;
    }
}
