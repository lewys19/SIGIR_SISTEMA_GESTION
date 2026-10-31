package pe.sigir.auditoria;

/** Puerto de auditoría: los servicios dependen de esto, no de la implementación. */
public interface Auditor {
    void registrar(String actor, String accion, String entidad, String entidadId, String detalle);
}
