package pe.sigir.monitoreo;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.stream.StreamCommands;
import io.quarkus.redis.datasource.stream.StreamRange;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pe.sigir.eventos.ConexionesWs;
import pe.sigir.roles.Permiso;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/api/monitoreo")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(Permiso.MONITOREO_VER)
public class MonitoreoResource {

    public record Estado(long eventosEnStream, int conexionesActivas) {
    }

    private final StreamCommands<String, String, String> streams;
    private final String stream;
    private final ConexionesWs conexiones;

    public MonitoreoResource(RedisDataSource redis, @ConfigProperty(name = "sigir.eventos.stream") String stream,
                             ConexionesWs conexiones) {
        this.streams = redis.stream(String.class);
        this.stream = stream;
        this.conexiones = conexiones;
    }

    /** Últimos eventos del stream, del más nuevo al más viejo. */
    @GET
    @Path("/eventos")
    public List<Map<String, Object>> eventos(@QueryParam("limite") @DefaultValue("50") int limite) {
        return streams.xrevrange(stream, StreamRange.of("+", "-"), Math.clamp(limite, 1, 200)).stream()
                .map(m -> {
                    Map<String, Object> e = new LinkedHashMap<>();
                    e.put("id", m.id());
                    e.put("tipo", m.payload().get("tipo"));
                    e.put("incidenteId", Long.valueOf(m.payload().get("incidenteId")));
                    e.put("codigo", m.payload().get("codigo"));
                    e.put("titulo", m.payload().get("titulo"));
                    e.put("severidad", m.payload().get("severidad"));
                    e.put("actor", m.payload().get("actor"));
                    e.put("mensaje", m.payload().get("mensaje"));
                    e.put("fecha", m.payload().get("fecha"));
                    return e;
                })
                .toList();
    }

    @GET
    @Path("/estado")
    public Estado estado() {
        return new Estado(streams.xlen(stream), conexiones.activas());
    }
}
