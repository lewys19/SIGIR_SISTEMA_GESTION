package pe.sigir.auditoria;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import pe.sigir.compartido.Pagina;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AuditoriaRepository implements PanacheRepository<RegistroAuditoria>, AuditoriaDao {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    @Override
    public void insertar(RegistroAuditoria registro) {
        persist(registro);
    }

    @Override
    public Pagina<RegistroAuditoria> buscar(FiltroAuditoria f) {
        List<String> condiciones = new ArrayList<>();
        Parameters p = new Parameters();
        if (f.accion() != null && !f.accion().isBlank()) {
            condiciones.add("accion = :accion");
            p.and("accion", f.accion());
        }
        if (f.actor() != null && !f.actor().isBlank()) {
            condiciones.add("lower(actor) like :actor");
            p.and("actor", "%" + f.actor().toLowerCase() + "%");
        }
        if (f.desde() != null) {
            condiciones.add("fecha >= :desde");
            p.and("desde", f.desde().atStartOfDay(LIMA).toOffsetDateTime());
        }
        if (f.hasta() != null) {
            condiciones.add("fecha < :hasta");
            p.and("hasta", f.hasta().plusDays(1).atStartOfDay(LIMA).toOffsetDateTime());
        }
        String where = condiciones.isEmpty() ? "1 = 1" : String.join(" and ", condiciones);
        PanacheQuery<RegistroAuditoria> q = find(where, Sort.descending("fecha", "id"), p)
                .page(Page.of(f.pagina(), f.tamano()));
        return new Pagina<>(q.list(), q.count(), f.pagina(), f.tamano());
    }

    @Override
    public List<String> acciones() {
        return getEntityManager()
                .createQuery("select distinct a.accion from RegistroAuditoria a order by a.accion", String.class)
                .getResultList();
    }
}
