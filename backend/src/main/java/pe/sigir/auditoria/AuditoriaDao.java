package pe.sigir.auditoria;

import pe.sigir.compartido.Pagina;

import java.util.List;

public interface AuditoriaDao {
    void insertar(RegistroAuditoria registro);

    Pagina<RegistroAuditoria> buscar(FiltroAuditoria filtro);

    List<String> acciones();
}
