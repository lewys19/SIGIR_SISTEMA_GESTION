package pe.sigir.notificaciones;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import pe.sigir.eventos.EventoIncidente;
import pe.sigir.eventos.EventoListoParaPublicar;
import pe.sigir.roles.MatrizPermisos;
import pe.sigir.roles.Permiso;
import pe.sigir.usuarios.Usuario;
import pe.sigir.usuarios.UsuarioDao;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Escucha los eventos de dominio DENTRO de la transacción del caso de uso:
 * si la operación hace rollback, tampoco quedan notificaciones huérfanas.
 */
@ApplicationScoped
public class NotificacionesListener {

    private final NotificacionDao dao;
    private final UsuarioDao usuarios;
    private final MatrizPermisos matriz;
    private final Event<EventoListoParaPublicar> publicar;

    public NotificacionesListener(NotificacionDao dao, UsuarioDao usuarios, MatrizPermisos matriz,
                                  Event<EventoListoParaPublicar> publicar) {
        this.dao = dao;
        this.usuarios = usuarios;
        this.matriz = matriz;
        this.publicar = publicar;
    }

    void alOcurrir(@Observes EventoIncidente e) {
        Set<Long> destinatarios = destinatarios(e);
        String titulo = titulo(e);
        for (Long id : destinatarios) {
            Notificacion n = new Notificacion();
            n.usuarioId = id;
            n.tipo = e.tipo().name();
            n.titulo = titulo;
            n.mensaje = e.comentario() != null && !e.comentario().isBlank() ? e.comentario() : e.titulo();
            n.incidenteId = e.incidenteId();
            dao.guardar(n);
        }
        // Se publicará en Redis solo cuando la transacción se confirme
        publicar.fire(new EventoListoParaPublicar(e, destinatarios, titulo));
    }

    private Set<Long> destinatarios(EventoIncidente e) {
        Set<Long> ids = new LinkedHashSet<>();
        switch (e.tipo()) {
            case INCIDENTE_ENVIADO -> usuarios.activosConRol(matriz.rolesCon(Permiso.REVISION_APROBAR))
                    .stream().map((Usuario u) -> u.id).forEach(ids::add);
            case INCIDENTE_APROBADO, INCIDENTE_RECHAZADO, INCIDENTE_CERRADO -> ids.add(e.reportadoPorId());
            default -> {
            }
        }
        ids.remove(e.actorId());
        return ids;
    }

    static String titulo(EventoIncidente e) {
        return switch (e.tipo()) {
            case INCIDENTE_CREADO -> "Nuevo incidente registrado: " + e.codigo();
            case INCIDENTE_ACTUALIZADO -> "Incidente actualizado: " + e.codigo();
            case INCIDENTE_ENVIADO -> "Incidente por revisar: " + e.codigo();
            case INCIDENTE_APROBADO -> "Tu incidente " + e.codigo() + " fue aprobado";
            case INCIDENTE_RECHAZADO -> "Tu incidente " + e.codigo() + " fue rechazado";
            case INCIDENTE_CERRADO -> "Tu incidente " + e.codigo() + " fue cerrado";
        };
    }
}
