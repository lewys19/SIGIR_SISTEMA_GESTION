package pe.sigir.dashboard;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;
import pe.sigir.soporte.Api;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static pe.sigir.incidentes.IncidentesResourceTest.datos;

@QuarkusTest
class DashboardResourceTest {

    private JsonPath kpis(Api.Sesion s) {
        return s.req().get("/api/dashboard").then().statusCode(200).extract().jsonPath();
    }

    @Test
    void unIncidenteCriticoVencidoMueveLosIndicadores() {
        Api.Sesion analista = Api.usuario("ANALISTA");
        JsonPath antes = kpis(analista);

        Map<String, Object> d = datos("Crítico vencido");
        d.put("severidad", "CRITICA");
        d.put("fechaOcurrencia", LocalDate.now().minusDays(12).toString());
        d.put("fechaDeteccion", LocalDate.now().minusDays(10).toString());
        analista.req().body(d).post("/api/incidentes").then().statusCode(201);

        JsonPath despues = kpis(analista);
        assertEquals(antes.getInt("kpis.total") + 1, despues.getInt("kpis.total"));
        assertEquals(antes.getInt("kpis.abiertos") + 1, despues.getInt("kpis.abiertos"));
        assertEquals(antes.getInt("kpis.criticosAbiertos") + 1, despues.getInt("kpis.criticosAbiertos"));
        assertEquals(antes.getInt("kpis.vencidos") + 1, despues.getInt("kpis.vencidos"));
    }

    @Test
    void traeSeriesParaLosGraficos() {
        Api.admin().req().get("/api/dashboard").then().statusCode(200)
                .body("porMes", hasSize(6))
                .body("porMes[5].mes", is(LocalDate.now().toString().substring(0, 7)))
                .body("porSeveridad.severidad", hasItems("BAJA", "MEDIA", "ALTA", "CRITICA"))
                .body("porEstado.estado", hasItem("BORRADOR"))
                .body("urgentes", hasSize(lessThanOrEqualTo(5)));
    }

    @Test
    void sinPermisoNoHayDashboard() {
        io.restassured.RestAssured.given().get("/api/dashboard").then().statusCode(401);
    }
}
