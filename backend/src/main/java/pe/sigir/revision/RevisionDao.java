package pe.sigir.revision;

import java.util.List;

public interface RevisionDao {
    void guardar(Revision revision);

    List<Revision> historialDe(Long incidenteId);
}
