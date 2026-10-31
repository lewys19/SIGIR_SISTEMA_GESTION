package pe.sigir.monitoreo;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocket;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import pe.sigir.soporte.Api;

import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static pe.sigir.incidentes.IncidentesResourceTest.crear;

@QuarkusTest
class MonitoreoTest {

    @Inject
    Vertx vertx;

    @TestHTTPResource("/")
    URL base;

    private WebSocket conectar(String ticket, BlockingQueue<String> mensajes, CompletableFuture<Void> cerrado)
            throws Exception {
        WebSocket ws = vertx.createWebSocketClient()
                .connect(base.getPort(), base.getHost(), "/ws/eventos?ticket=" + ticket)
                .toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        ws.textMessageHandler(mensajes::add);
        ws.closeHandler(v -> cerrado.complete(null));
        return ws;
    }

    private String ticket(Api.Sesion s) {
        return s.req().post("/api/notificaciones/ticket").then().statusCode(200).extract().path("ticket");
    }

    @Test
    void eventosQuedanEnElStreamYSoloLosVeQuienTienePermiso() {
        Api.Sesion analista = Api.usuario("ANALISTA");
        long id = crear(analista, "Monitoreado");
        String codigo = analista.req().get("/api/incidentes/" + id).then().extract().path("codigo");

        Api.usuario("AUDITOR").req().get("/api/monitoreo/eventos").then().statusCode(200)
                .body("find { it.codigo == '" + codigo + "' }.tipo", is("INCIDENTE_CREADO"));
        analista.req().get("/api/monitoreo/eventos").then().statusCode(403);
    }

    @Test
    void elSupervisorRecibeEventoYNotificacionPorWebSocket() throws Exception {
        Api.Sesion supervisor = Api.usuario("SUPERVISOR");
        Api.Sesion analista = Api.usuario("ANALISTA");
        BlockingQueue<String> mensajes = new LinkedBlockingQueue<>();
        WebSocket ws = conectar(ticket(supervisor), mensajes, new CompletableFuture<>());
        try {
            Thread.sleep(300);
            long id = crear(analista, "En vivo");
            analista.req().post("/api/incidentes/" + id + "/enviar").then().statusCode(200);

            boolean evento = false;
            boolean notificacion = false;
            long limite = System.currentTimeMillis() + 8000;
            while ((!evento || !notificacion) && System.currentTimeMillis() < limite) {
                String m = mensajes.poll(500, TimeUnit.MILLISECONDS);
                if (m == null || !m.contains("\"incidenteId\":" + id + ",")) {
                    continue;
                }
                evento |= m.contains("\"canal\":\"evento\"");
                notificacion |= m.contains("\"canal\":\"notificacion\"") && m.contains("INCIDENTE_ENVIADO");
            }
            assertTrue(evento, "no llegó el evento de monitoreo");
            assertTrue(notificacion, "no llegó la notificación");
        } finally {
            ws.close();
        }
    }

    @Test
    void ticketInvalidoCierraLaConexion() throws Exception {
        CompletableFuture<Void> cerrado = new CompletableFuture<>();
        conectar("inventado", new LinkedBlockingQueue<>(), cerrado);
        cerrado.get(5, TimeUnit.SECONDS);
    }

    @Test
    void elTicketSoloSirveUnaVez() throws Exception {
        String t = ticket(Api.usuario("SUPERVISOR"));
        WebSocket primero = conectar(t, new LinkedBlockingQueue<>(), new CompletableFuture<>());
        CompletableFuture<Void> cerrado = new CompletableFuture<>();
        conectar(t, new LinkedBlockingQueue<>(), cerrado);
        cerrado.get(5, TimeUnit.SECONDS);
        assertFalse(primero.isClosed());
        primero.close();
    }
}
