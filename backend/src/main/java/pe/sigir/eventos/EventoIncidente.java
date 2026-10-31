package pe.sigir.eventos;

import pe.sigir.incidentes.Incidente;

import java.time.Instant;

/** Evento de dominio: algo le pasó a un incidente. Lo escuchan notificaciones y monitoreo. */
public record EventoIncidente(TipoEvento tipo, Long incidenteId, String codigo, String titulo, String severidad,
                              Long actorId, String actorEmail, Long reportadoPorId, String comentario,
                              Instant fecha) {

    public static EventoIncidente de(TipoEvento tipo, Incidente i, Long actorId, String actorEmail, String comentario) {
        return new EventoIncidente(tipo, i.id, i.codigo, i.titulo, i.severidad.name(), actorId, actorEmail,
                i.reportadoPor.id, comentario, Instant.now());
    }
}
