package pe.sigir.eventos;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.stream.StreamCommands;
import io.quarkus.redis.datasource.stream.StreamMessage;
import io.quarkus.redis.datasource.stream.StreamRange;
import io.quarkus.redis.datasource.stream.XReadArgs;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.List;

/** Hilo que lee el Redis Stream y reparte cada evento por WebSocket. */
@ApplicationScoped
public class ConsumidorEventos {

    private static final Logger LOG = Logger.getLogger(ConsumidorEventos.class);

    private final StreamCommands<String, String, String> streams;
    private final String stream;
    private final ConexionesWs conexiones;
    private volatile boolean activo;
    private Thread hilo;

    public ConsumidorEventos(RedisDataSource redis, @ConfigProperty(name = "sigir.eventos.stream") String stream,
                             ConexionesWs conexiones) {
        this.streams = redis.stream(String.class);
        this.stream = stream;
        this.conexiones = conexiones;
    }

    void iniciar(@Observes StartupEvent e) {
        activo = true;
        hilo = Thread.ofPlatform().daemon().name("sigir-eventos").start(this::bucle);
    }

    void detener(@Observes ShutdownEvent e) {
        activo = false;
        if (hilo != null) {
            hilo.interrupt();
        }
    }

    private void bucle() {
        String ultimo = null;
        while (activo) {
            try {
                if (ultimo == null) {
                    ultimo = ultimoId();
                }
                List<StreamMessage<String, String, String>> mensajes = streams.xread(stream, ultimo,
                        new XReadArgs().block(Duration.ofSeconds(1)).count(100));
                for (StreamMessage<String, String, String> m : mensajes) {
                    ultimo = m.id();
                    conexiones.difundir(m.payload());
                }
            } catch (Exception ex) {
                if (!activo) {
                    break;
                }
                LOG.warnf("Lectura del stream falló, reintento en 2 s: %s", ex.getMessage());
                dormir();
            }
        }
    }

    /** Arranca desde el último evento existente: no reenvía historia vieja. */
    private String ultimoId() {
        List<StreamMessage<String, String, String>> ultimo = streams.xrevrange(stream, StreamRange.of("+", "-"), 1);
        return ultimo.isEmpty() ? "0-0" : ultimo.getFirst().id();
    }

    private static void dormir() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
