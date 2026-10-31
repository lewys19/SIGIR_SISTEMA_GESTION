package pe.sigir.revision;

import jakarta.enterprise.context.ApplicationScoped;
import pe.sigir.usuarios.Usuario;

@ApplicationScoped
public class HistorialRevisiones {

    private final RevisionDao dao;

    public HistorialRevisiones(RevisionDao dao) {
        this.dao = dao;
    }

    public void registrar(Long incidenteId, Usuario usuario, DecisionRevision decision, String comentario) {
        Revision r = new Revision();
        r.incidenteId = incidenteId;
        r.revisor = usuario;
        r.decision = decision;
        r.comentario = comentario == null || comentario.isBlank() ? null : comentario.trim();
        dao.guardar(r);
    }
}
