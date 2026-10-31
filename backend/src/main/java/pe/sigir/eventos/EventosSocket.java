package pe.sigir.eventos;

import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import pe.sigir.notificaciones.TicketsWs;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/** ws://…/ws/eventos?ticket=… — canal en vivo de notificaciones y monitoreo. */
@WebSocket(path = "/ws/eventos")
public class EventosSocket {

    private final TicketsWs tickets;
    private final ConexionesWs conexiones;

    public EventosSocket(TicketsWs tickets, ConexionesWs conexiones) {
        this.tickets = tickets;
        this.conexiones = conexiones;
    }

    @OnOpen
    public void abrir(WebSocketConnection ws) {
        tickets.consumir(ticketDe(ws.handshakeRequest().query())).ifPresentOrElse(
                t -> conexiones.agregar(ws, t.usuarioId(), t.rol()),
                ws::closeAndAwait);
    }

    @OnClose
    public void cerrar(WebSocketConnection ws) {
        conexiones.quitar(ws.id());
    }

    static String ticketDe(String query) {
        if (query == null) {
            return null;
        }
        for (String par : query.split("&")) {
            if (par.startsWith("ticket=")) {
                return URLDecoder.decode(par.substring(7), StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
