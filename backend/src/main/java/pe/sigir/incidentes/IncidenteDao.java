package pe.sigir.incidentes;

import pe.sigir.compartido.Pagina;

import java.util.List;
import java.util.Optional;

public interface IncidenteDao {
    Optional<Incidente> buscarPorId(Long id);

    Pagina<Incidente> buscar(FiltroIncidentes filtro);

    List<Incidente> enEstado(EstadoIncidente estado);

    void guardar(Incidente incidente);

    long siguienteCorrelativo();
}
