package pe.sigir.revision;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import pe.sigir.auditoria.Auditor;
import pe.sigir.compartido.NegocioException;
import pe.sigir.compartido.PeticionInvalidaException;
import pe.sigir.compartido.UsuarioActual;
import pe.sigir.eventos.TipoEvento;
import pe.sigir.incidentes.EstadoIncidente;
import pe.sigir.incidentes.Incidente;
import pe.sigir.incidentes.IncidenteDao;
import pe.sigir.incidentes.IncidenteDtos.IncidenteDetalle;
import pe.sigir.incidentes.IncidenteDtos.IncidenteResumen;
import pe.sigir.incidentes.IncidenteService;

import java.util.List;

@ApplicationScoped
public class RevisionService {

    private final IncidenteDao incidentes;
    private final IncidenteService servicioIncidentes;
    private final HistorialRevisiones historial;
    private final Auditor auditor;
    private final UsuarioActual actual;

    public RevisionService(IncidenteDao incidentes, IncidenteService servicioIncidentes,
                           HistorialRevisiones historial, Auditor auditor, UsuarioActual actual) {
        this.incidentes = incidentes;
        this.servicioIncidentes = servicioIncidentes;
        this.historial = historial;
        this.auditor = auditor;
        this.actual = actual;
    }

    public List<IncidenteResumen> pendientes() {
        return incidentes.enEstado(EstadoIncidente.EN_REVISION).stream().map(IncidenteResumen::de).toList();
    }

    @Transactional
    public IncidenteDetalle aprobar(Long id, String comentario) {
        Incidente i = servicioIncidentes.obtener(id);
        exigirSegregacion(i);
        i.aprobar();
        return registrar(i, DecisionRevision.APROBADO, TipoEvento.INCIDENTE_APROBADO, comentario);
    }

    @Transactional
    public IncidenteDetalle rechazar(Long id, String comentario) {
        if (comentario == null || comentario.isBlank()) {
            throw new PeticionInvalidaException("Para rechazar hay que indicar el motivo");
        }
        Incidente i = servicioIncidentes.obtener(id);
        exigirSegregacion(i);
        i.rechazar();
        return registrar(i, DecisionRevision.RECHAZADO, TipoEvento.INCIDENTE_RECHAZADO, comentario);
    }

    @Transactional
    public IncidenteDetalle cerrar(Long id, String comentario) {
        Incidente i = servicioIncidentes.obtener(id);
        i.cerrar();
        return registrar(i, DecisionRevision.CERRADO, TipoEvento.INCIDENTE_CERRADO, comentario);
    }

    /** Segregación de funciones: quien registra no puede aprobar ni rechazar. */
    private void exigirSegregacion(Incidente i) {
        if (i.reportadoPor.id.equals(actual.id())) {
            throw new NegocioException("No puedes revisar tu propio incidente");
        }
    }

    private IncidenteDetalle registrar(Incidente i, DecisionRevision decision, TipoEvento evento, String comentario) {
        historial.registrar(i.id, servicioIncidentes.yo(), decision, comentario);
        auditor.registrar(actual.email(), evento.name(), "INCIDENTE", String.valueOf(i.id),
                i.codigo + (comentario == null || comentario.isBlank() ? "" : " — " + comentario.trim()));
        servicioIncidentes.publicar(evento, i, comentario);
        return servicioIncidentes.detalle(i);
    }
}
