package pe.sigir.reportes;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import pe.sigir.soporte.Api;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static pe.sigir.incidentes.IncidentesResourceTest.datos;

@QuarkusTest
class ReportesResourceTest {

    @Test
    void exportarCsvConFiltrosYProteccionContraFormulas() {
        Api.Sesion analista = Api.usuario("ANALISTA");
        Map<String, Object> d = datos("=HYPERLINK(\"http://malo\")");
        d.put("regulador", "SBS");
        String codigo = analista.req().body(d).post("/api/incidentes").then().statusCode(201)
                .extract().path("codigo");

        String hoy = LocalDate.now().toString();
        String csv = Api.usuario("AUDITOR").req()
                .queryParam("desde", LocalDate.now().minusDays(30).toString()).queryParam("hasta", hoy)
                .get("/api/reportes/incidentes.csv").then().statusCode(200)
                .contentType(containsString("text/csv"))
                .header("Content-Disposition", containsString("attachment"))
                .extract().asString();

        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("Código;Título;Tipo"), csv.substring(0, 80));
        org.junit.jupiter.api.Assertions.assertTrue(csv.contains(codigo));
        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("'=HYPERLINK"), "no neutralizó la fórmula");

        Api.admin().req().queryParam("accion", "REPORTE_EXPORTADO").get("/api/auditoria")
                .then().body("total", greaterThanOrEqualTo(1));
    }

    @Test
    void resumenPorReguladorYTipo() {
        Api.Sesion analista = Api.usuario("ANALISTA");
        Map<String, Object> d = datos("Para resumen");
        d.put("regulador", "SMV");
        analista.req().body(d).post("/api/incidentes").then().statusCode(201);

        Api.admin().req().queryParam("desde", LocalDate.now().minusDays(30).toString())
                .get("/api/reportes/resumen").then().statusCode(200)
                .body("total", greaterThanOrEqualTo(1))
                .body("porRegulador.find { it.regulador == 'SMV' }.total", greaterThanOrEqualTo(1))
                .body("porTipo.tipo", hasItem("PROTECCION_DATOS"))
                .body("cumplimiento", notNullValue());
    }

    @Test
    void analistaNoAccedeAReportes() {
        Api.usuario("ANALISTA").req().get("/api/reportes/resumen").then().statusCode(403);
    }

    @Test
    void rangoInvertidoDa400() {
        Api.admin().req().queryParam("desde", "2026-10-10").queryParam("hasta", "2026-10-01")
                .get("/api/reportes/resumen").then().statusCode(400);
    }
}
