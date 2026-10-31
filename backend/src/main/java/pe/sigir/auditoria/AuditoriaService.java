package pe.sigir.auditoria;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import pe.sigir.compartido.Textos;

/**
 * Auditoría síncrona: se une a la transacción del caso de uso (REQUIRED).
 * Si la operación falla y hace rollback, su registro de auditoría también desaparece:
 * nunca queda auditada una acción que no ocurrió.
 */
@ApplicationScoped
public class AuditoriaService implements Auditor {

    private final AuditoriaDao dao;

    public AuditoriaService(AuditoriaDao dao) {
        this.dao = dao;
    }

    @Override
    @Transactional
    public void registrar(String actor, String accion, String entidad, String entidadId, String detalle) {
        RegistroAuditoria r = new RegistroAuditoria();
        r.actor = actor;
        r.accion = accion;
        r.entidad = entidad;
        r.entidadId = entidadId;
        r.detalle = Textos.resumir(detalle, 2000);   // ningún registro puede crecer sin límite
        dao.insertar(r);
    }
}
