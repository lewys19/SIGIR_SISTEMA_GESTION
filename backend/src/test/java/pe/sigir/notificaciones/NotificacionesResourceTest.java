package pe.sigir.notificaciones;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import pe.sigir.soporte.Api;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static pe.sigir.incidentes.IncidentesResourceTest.crear;

@QuarkusTest
class NotificacionesResourceTest {

    @Test
    void revisoresSeEnteranYElAutorRecibeLaDecision() {
        Api.Sesion analista = Api.usuario("ANALISTA");
        Api.Sesion supervisor = Api.usuario("SUPERVISOR");
        long id = crear(analista, "Notificar");
        analista.req().post("/api/incidentes/" + id + "/enviar").then().statusCode(200);

        supervisor.req().get("/api/notificaciones").then().statusCode(200)
                .body("noLeidas", greaterThanOrEqualTo(1))
                .body("items.find { it.incidenteId == " + id + " }.tipo", is("INCIDENTE_ENVIADO"));
        analista.req().get("/api/notificaciones").then().body("noLeidas", is(0));

        supervisor.req().body(Map.of("comentario", "ok")).post("/api/revisiones/" + id + "/aprobar")
                .then().statusCode(200);

        Number notifId = analista.req().get("/api/notificaciones").then()
                .body("noLeidas", is(1))
                .body("items[0].tipo", is("INCIDENTE_APROBADO"))
                .body("items[0].titulo", containsString("aprobado"))
                .extract().path("items[0].id");

        supervisor.req().post("/api/notificaciones/" + notifId + "/leer").then().statusCode(404);
        analista.req().post("/api/notificaciones/" + notifId + "/leer").then().statusCode(204);
        analista.req().get("/api/notificaciones").then().body("noLeidas", is(0));
    }

    @Test
    void marcarTodasComoLeidas() {
        Api.Sesion analista = Api.usuario("ANALISTA");
        Api.Sesion supervisor = Api.usuario("SUPERVISOR");
        for (int n = 0; n < 2; n++) {
            long id = crear(analista, "Masivo " + n);
            analista.req().post("/api/incidentes/" + id + "/enviar").then().statusCode(200);
        }
        supervisor.req().get("/api/notificaciones").then().body("noLeidas", greaterThanOrEqualTo(2));
        supervisor.req().post("/api/notificaciones/leer-todas").then().statusCode(204);
        supervisor.req().get("/api/notificaciones").then().body("noLeidas", is(0));
    }

    @Test
    void sinSesionNoHayNotificaciones() {
        io.restassured.RestAssured.given().get("/api/notificaciones").then().statusCode(401);
    }
}
