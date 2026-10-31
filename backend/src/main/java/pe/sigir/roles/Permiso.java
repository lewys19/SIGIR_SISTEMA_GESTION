package pe.sigir.roles;

import java.util.List;

/** Catálogo de permisos. Se usan como "roles" en @RolesAllowed gracias a PermisosAugmentor. */
public final class Permiso {

    public static final String DASHBOARD_VER = "dashboard:ver";
    public static final String INCIDENTES_VER = "incidentes:ver";
    public static final String INCIDENTES_CREAR = "incidentes:crear";
    public static final String INCIDENTES_EDITAR = "incidentes:editar";
    public static final String REVISION_APROBAR = "revision:aprobar";
    public static final String MONITOREO_VER = "monitoreo:ver";
    public static final String REPORTES_VER = "reportes:ver";
    public static final String USUARIOS_GESTIONAR = "usuarios:gestionar";
    public static final String ROLES_GESTIONAR = "roles:gestionar";
    public static final String AUDITORIA_VER = "auditoria:ver";

    public static final List<String> CATALOGO = List.of(
            DASHBOARD_VER, INCIDENTES_VER, INCIDENTES_CREAR, INCIDENTES_EDITAR, REVISION_APROBAR,
            MONITOREO_VER, REPORTES_VER, USUARIOS_GESTIONAR, ROLES_GESTIONAR, AUDITORIA_VER);

    private Permiso() {
    }
}
