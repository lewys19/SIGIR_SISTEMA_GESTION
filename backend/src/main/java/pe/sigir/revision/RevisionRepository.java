package pe.sigir.revision;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class RevisionRepository implements PanacheRepository<Revision>, RevisionDao {

    @Override
    public void guardar(Revision revision) {
        persist(revision);
    }

    @Override
    public List<Revision> historialDe(Long incidenteId) {
        return list("incidenteId", Sort.ascending("fecha", "id"), incidenteId);
    }
}
