package pe.sigir.notificaciones;

import java.util.List;
import java.util.Optional;

public interface NotificacionDao {
    void guardar(Notificacion n);

    List<Notificacion> recientesDe(Long usuarioId, int limite);

    long noLeidasDe(Long usuarioId);

    Optional<Notificacion> buscarDe(Long id, Long usuarioId);

    void marcarTodasLeidas(Long usuarioId);
}
