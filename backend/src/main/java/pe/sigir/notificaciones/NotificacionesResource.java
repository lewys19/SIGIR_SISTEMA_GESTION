package pe.sigir.notificaciones;

import io.quarkus.security.Authenticated;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import pe.sigir.compartido.NoEncontradoException;
import pe.sigir.compartido.UsuarioActual;

import java.time.OffsetDateTime;
import java.util.List;

@Path("/api/notificaciones")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class NotificacionesResource {

    public record NotificacionResponse(Long id, String tipo, String titulo, String mensaje, Long incidenteId,
                                       boolean leida, OffsetDateTime creadaEn) {
        static NotificacionResponse de(Notificacion n) {
            return new NotificacionResponse(n.id, n.tipo, n.titulo, n.mensaje, n.incidenteId, n.leida, n.creadaEn);
        }
    }

    public record Bandeja(long noLeidas, List<NotificacionResponse> items) {
    }

    public record TicketResponse(String ticket) {
    }

    private final NotificacionDao dao;
    private final UsuarioActual actual;
    private final TicketsWs tickets;
    private final JsonWebToken jwt;

    public NotificacionesResource(NotificacionDao dao, UsuarioActual actual, TicketsWs tickets, JsonWebToken jwt) {
        this.dao = dao;
        this.actual = actual;
        this.tickets = tickets;
        this.jwt = jwt;
    }

    @GET
    public Bandeja bandeja() {
        Long yo = actual.id();
        return new Bandeja(dao.noLeidasDe(yo), dao.recientesDe(yo, 30).stream().map(NotificacionResponse::de).toList());
    }

    @POST
    @Path("/{id}/leer")
    @Transactional
    public void leer(@PathParam("id") Long id) {
        dao.buscarDe(id, actual.id())
                .orElseThrow(() -> new NoEncontradoException("Notificación no encontrada")).leida = true;
    }

    @POST
    @Path("/leer-todas")
    @Transactional
    public void leerTodas() {
        dao.marcarTodasLeidas(actual.id());
    }

    @POST
    @Path("/ticket")
    public TicketResponse ticket() {
        String rol = jwt.getGroups().stream().findFirst().orElse("");
        return new TicketResponse(tickets.emitir(actual.id(), rol));
    }
}
