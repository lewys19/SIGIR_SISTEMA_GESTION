package pe.sigir.incidentes;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import pe.sigir.compartido.Pagina;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class IncidenteRepository implements PanacheRepository<Incidente>, IncidenteDao {

    @Override
    public Optional<Incidente> buscarPorId(Long id) {
        return findByIdOptional(id);
    }

    @Override
    public Pagina<Incidente> buscar(FiltroIncidentes f) {
        List<String> c = new ArrayList<>();
        Parameters p = new Parameters();
        if (f.estado() != null) {
            c.add("estado = :estado");
            p.and("estado", f.estado());
        }
        if (f.severidad() != null) {
            c.add("severidad = :severidad");
            p.and("severidad", f.severidad());
        }
        if (f.tipo() != null) {
            c.add("tipo = :tipo");
            p.and("tipo", f.tipo());
        }
        if (f.texto() != null && !f.texto().isBlank()) {
            c.add("(lower(titulo) like :texto or lower(codigo) like :texto)");
            p.and("texto", "%" + f.texto().trim().toLowerCase() + "%");
        }
        String where = c.isEmpty() ? "1 = 1" : String.join(" and ", c);
        PanacheQuery<Incidente> q = find(where, Sort.descending("creadoEn", "id"), p)
                .page(Page.of(f.pagina(), f.tamano()));
        return new Pagina<>(q.list(), q.count(), f.pagina(), f.tamano());
    }

    @Override
    public List<Incidente> enEstado(EstadoIncidente estado) {
        return list("estado", Sort.ascending("fechaLimiteReporte"), estado);
    }

    @Override
    public void guardar(Incidente incidente) {
        persist(incidente);
    }

    @Override
    public long siguienteCorrelativo() {
        return ((Number) getEntityManager().createNativeQuery("SELECT nextval('incidente_codigo_seq')")
                .getSingleResult()).longValue();
    }
}
