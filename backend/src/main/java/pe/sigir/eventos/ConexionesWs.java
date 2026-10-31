package pe.sigir.eventos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import pe.sigir.roles.MatrizPermisos;
import pe.sigir.roles.Permiso;
import pe.sigir.usuarios.Rol;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** Navegadores conectados y a quién le toca recibir cada evento. */
@ApplicationScoped
public class ConexionesWs {

    private static final Logger LOG = Logger.getLogger(ConexionesWs.class);

    record Conexion(WebSocketConnection ws, Long usuarioId, Rol rol) {
    }

    private final Map<String, Conexion> conexiones = new ConcurrentHashMap<>();
    private final MatrizPermisos matriz;
    private final ObjectMapper json;

    public ConexionesWs(MatrizPermisos matriz, ObjectMapper json) {
        this.matriz = matriz;
        this.json = json;
    }

    public void agregar(WebSocketConnection ws, Long usuarioId, String rol) {
        conexiones.put(ws.id(), new Conexion(ws, usuarioId, Rol.valueOf(rol)));
    }

    public void quitar(String id) {
        conexiones.remove(id);
    }

    public int activas() {
        return conexiones.size();
    }

    public void difundir(Map<String, String> campos) {
        Set<Long> destinatarios = Arrays.stream(campos.getOrDefault("destinatarios", "").split(","))
                .filter(s -> !s.isBlank()).map(Long::valueOf).collect(Collectors.toSet());
        for (Conexion c : conexiones.values()) {
            if (destinatarios.contains(c.usuarioId())) {
                enviar(c, "notificacion", campos);
            }
            if (matriz.permisosDe(c.rol()).contains(Permiso.MONITOREO_VER)) {
                enviar(c, "evento", campos);
            }
        }
    }

    private void enviar(Conexion c, String canal, Map<String, String> campos) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("canal", canal);
        m.put("tipo", campos.get("tipo"));
        m.put("incidenteId", Long.valueOf(campos.get("incidenteId")));
        m.put("codigo", campos.get("codigo"));
        m.put("titulo", campos.get("titulo"));
        m.put("severidad", campos.get("severidad"));
        m.put("actor", campos.get("actor"));
        m.put("mensaje", campos.get("mensaje"));
        m.put("fecha", campos.get("fecha"));
        try {
            c.ws().sendText(json.writeValueAsString(m)).subscribe().with(
                    ok -> { },
                    err -> LOG.debugf("No se pudo enviar a %s: %s", c.ws().id(), err.getMessage()));
        } catch (JsonProcessingException ex) {
            LOG.warn("Evento no serializable", ex);
        }
    }
}
