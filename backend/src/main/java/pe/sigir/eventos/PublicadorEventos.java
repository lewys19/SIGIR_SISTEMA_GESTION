package pe.sigir.eventos;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.stream.StreamCommands;
import io.quarkus.redis.datasource.stream.XAddArgs;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Publica en el Redis Stream SOLO después del commit: nunca se anuncia algo que no se guardó. */
@ApplicationScoped
public class PublicadorEventos {

    private static final Logger LOG = Logger.getLogger(PublicadorEventos.class);

    private final StreamCommands<String, String, String> streams;
    private final String stream;

    public PublicadorEventos(RedisDataSource redis, @ConfigProperty(name = "sigir.eventos.stream") String stream) {
        this.streams = redis.stream(String.class);
        this.stream = stream;
    }

    void alConfirmar(@Observes(during = TransactionPhase.AFTER_SUCCESS) EventoListoParaPublicar listo) {
        EventoIncidente e = listo.evento();
        Map<String, String> campos = new LinkedHashMap<>();
        campos.put("tipo", e.tipo().name());
        campos.put("incidenteId", String.valueOf(e.incidenteId()));
        campos.put("codigo", e.codigo());
        campos.put("titulo", e.titulo());
        campos.put("severidad", e.severidad());
        campos.put("actor", e.actorEmail());
        campos.put("mensaje", listo.titulo());
        campos.put("comentario", e.comentario() == null ? "" : e.comentario());
        campos.put("fecha", e.fecha().toString());
        campos.put("destinatarios", listo.destinatarios().stream().map(String::valueOf).collect(Collectors.joining(",")));
        try {
            streams.xadd(stream, new XAddArgs().maxlen(10_000L).nearlyExactTrimming(), campos);
        } catch (Exception ex) {
            // El dato ya está guardado en PostgreSQL: un fallo de Redis no debe romper la operación
            LOG.warnf("No se pudo publicar el evento %s en Redis: %s", e.tipo(), ex.getMessage());
        }
    }
}
