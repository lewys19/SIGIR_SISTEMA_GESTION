package pe.sigir.revision;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import pe.sigir.soporte.Api;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static pe.sigir.incidentes.IncidentesResourceTest.crear;
import static pe.sigir.incidentes.IncidentesResourceTest.datos;

@QuarkusTest
class RevisionResourceTest {

    @Test
    void flujoCompletoConRechazoYAprobacion() {
        Api.Sesion analista = Api.usuario("ANALISTA");
        Api.Sesion supervisor = Api.usuario("SUPERVISOR");
        long id = crear(analista, "Flujo completo");
        String base = "/api/incidentes/" + id;

        analista.req().post(base + "/enviar").then().statusCode(200).body("estado", is("EN_REVISION"));
        analista.req().body(datos("Editar en revisión")).put(base).then().statusCode(409);

        supervisor.req().get("/api/revisiones/pendientes").then().statusCode(200)
                .body("id", hasItem((int) id));

        supervisor.req().body(Map.of("comentario", "")).post("/api/revisiones/" + id + "/rechazar")
                .then().statusCode(400);
        supervisor.req().body(Map.of("comentario", "Falta el impacto económico"))
                .post("/api/revisiones/" + id + "/rechazar").then().statusCode(200).body("estado", is("RECHAZADO"));

        analista.req().body(datos("Con impacto económico")).put(base).then().statusCode(200);
        analista.req().post(base + "/enviar").then().statusCode(200);

        supervisor.req().body(Map.of("comentario", "Conforme")).post("/api/revisiones/" + id + "/aprobar")
                .then().statusCode(200).body("estado", is("APROBADO"));
        supervisor.req().body(Map.of()).post("/api/revisiones/" + id + "/cerrar")
                .then().statusCode(200).body("estado", is("CERRADO"));

        analista.req().get(base).then().statusCode(200)
                .body("historial.decision", contains("ENVIADO", "RECHAZADO", "ENVIADO", "APROBADO", "CERRADO"))
                .body("historial[1].comentario", is("Falta el impacto económico"))
                .body("acciones", empty());

        Api.admin().req().queryParam("accion", "INCIDENTE_APROBADO").queryParam("tamano", 100)
                .get("/api/auditoria").then().body("items.entidadId", hasItem(String.valueOf(id)));
    }

    @Test
    void nadieApruebaSuPropioIncidente() {
        Api.Sesion supervisor = Api.usuario("SUPERVISOR");
        long id = crear(supervisor, "Autoaprobación");
        supervisor.req().post("/api/incidentes/" + id + "/enviar").then().statusCode(200);
        supervisor.req().body(Map.of("comentario", "Yo mismo")).post("/api/revisiones/" + id + "/aprobar")
                .then().statusCode(409).body("mensaje", containsString("propio"));
    }

    @Test
    void analistaNoPuedeAprobar() {
        Api.Sesion autor = Api.usuario("ANALISTA");
        long id = crear(autor, "Sin permiso");
        autor.req().post("/api/incidentes/" + id + "/enviar").then().statusCode(200);
        Api.usuario("ANALISTA").req().body(Map.of("comentario", "ok"))
                .post("/api/revisiones/" + id + "/aprobar").then().statusCode(403);
    }

    @Test
    void soloElAutorEnviaARevision() {
        long id = crear(Api.usuario("ANALISTA"), "Ajeno");
        Api.usuario("ANALISTA").req().post("/api/incidentes/" + id + "/enviar").then().statusCode(403);
    }
}
