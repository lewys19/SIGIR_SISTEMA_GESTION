package pe.sigir.roles;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import pe.sigir.usuarios.Rol;

import java.util.*;

@ApplicationScoped
public class RolPermisoDaoJpa implements RolPermisoDao {

    private final EntityManager em;

    public RolPermisoDaoJpa(EntityManager em) {
        this.em = em;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Rol, Set<String>> cargarMatriz() {
        Map<Rol, Set<String>> matriz = new EnumMap<>(Rol.class);
        for (Rol r : Rol.values()) {
            matriz.put(r, new TreeSet<>());
        }
        List<Object[]> filas = em.createNativeQuery("SELECT rol, permiso FROM rol_permisos").getResultList();
        for (Object[] f : filas) {
            matriz.get(Rol.valueOf((String) f[0])).add((String) f[1]);
        }
        return matriz;
    }

    @Override
    public void reemplazar(Rol rol, Set<String> permisos) {
        em.createNativeQuery("DELETE FROM rol_permisos WHERE rol = ?1").setParameter(1, rol.name()).executeUpdate();
        for (String p : permisos) {
            em.createNativeQuery("INSERT INTO rol_permisos (rol, permiso) VALUES (?1, ?2)")
                    .setParameter(1, rol.name()).setParameter(2, p).executeUpdate();
        }
    }
}
