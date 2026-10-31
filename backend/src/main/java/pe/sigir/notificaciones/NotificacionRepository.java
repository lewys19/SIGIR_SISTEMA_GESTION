package pe.sigir.notificaciones;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class NotificacionRepository implements PanacheRepository<Notificacion>, NotificacionDao {

    @Override
    public void guardar(Notificacion n) {
        persist(n);
    }

    @Override
    public List<Notificacion> recientesDe(Long usuarioId, int limite) {
        return find("usuarioId", Sort.descending("creadaEn", "id"), usuarioId).page(Page.ofSize(limite)).list();
    }

    @Override
    public long noLeidasDe(Long usuarioId) {
        return count("usuarioId = ?1 and leida = false", usuarioId);
    }

    @Override
    public Optional<Notificacion> buscarDe(Long id, Long usuarioId) {
        return find("id = ?1 and usuarioId = ?2", id, usuarioId).firstResultOptional();
    }

    @Override
    public void marcarTodasLeidas(Long usuarioId) {
        update("leida = true where usuarioId = ?1 and leida = false", usuarioId);
    }
}
