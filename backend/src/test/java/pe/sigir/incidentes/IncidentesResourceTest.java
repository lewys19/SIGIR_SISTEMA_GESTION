package pe.sigir.incidentes;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import pe.sigir.soporte.Api;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@QuarkusTest
public class IncidentesResourceTest {

    public static Map<String, Object> datos(String titulo) {
        Map<String, Object> m = new HashMap<>();
        m.put("titulo", titulo);
        m.put("descripcion", "Se detectó acceso no autorizado a datos de clientes");
        m.put("tipo", "PROTECCION_DATOS");
        m.put("severidad", "ALTA");
        m.put("regulador", "ANPD");
        m.put("area", "TI");
        m.put("fechaOcurrencia", LocalDate.now().minusDays(2).toString());
        m.put("fechaDeteccion", LocalDate.now().minusDays(1).toString());
        m.put("impacto", "1200 registros expuestos");
        m.put("medidas", "Se revocaron credenciales");
        return m;
    }

    public static long crear(Api.Sesion s, String titulo) {
        return s.req().body(datos(titulo)).post("/api/incidentes").then().statusCode(201)
                .extract().<Number>path("id").longValue();
    }

    @Test
    void registrarIncidenteQuedaEnBorradorConCodigoYPlazo() {
        Api.Sesion analista = Api.usuario("ANALISTA");
        analista.req().body(datos("Fuga de datos")).post("/api/incidentes").then().statusCode(201)
                .body("codigo", matchesPattern("INC-\\d{4}-\\d{4}"))
                .body("estado", is("BORRADOR"))
                .body("fechaLimiteReporte", is(LocalDate.now().plusDays(2).toString()))
                .body("reportadoPor.email", is(analista.email()))
                .body("acciones", hasItems("editar", "enviar"));
    }

    @Test
    void fechaDeOcurrenciaFuturaDa400() {
        Map<String, Object> m = datos("Futuro");
        m.put("fechaOcurrencia", LocalDate.now().plusDays(3).toString());
        m.put("fechaDeteccion", LocalDate.now().plusDays(3).toString());
        Api.usuario("ANALISTA").req().body(m).post("/api/incidentes").then().statusCode(400);
    }

    @Test
    void deteccionAntesDeOcurrenciaDa400() {
        Map<String, Object> m = datos("Al revés");
        m.put("fechaOcurrencia", LocalDate.now().minusDays(1).toString());
        m.put("fechaDeteccion", LocalDate.now().minusDays(5).toString());
        Api.usuario("ANALISTA").req().body(m).post("/api/incidentes").then().statusCode(400);
    }

    @Test
    void camposObligatoriosDan400() {
        Api.usuario("ANALISTA").req().body(Map.of("titulo", "")).post("/api/incidentes").then().statusCode(400);
    }

    @Test
    void soloElAutorPuedeEditar() {
        Api.Sesion autor = Api.usuario("ANALISTA");
        long id = crear(autor, "Original");
        Api.usuario("ANALISTA").req().body(datos("Cambio ajeno")).put("/api/incidentes/" + id)
                .then().statusCode(403);
        autor.req().body(datos("Título corregido")).put("/api/incidentes/" + id)
                .then().statusCode(200).body("titulo", is("Título corregido"));
    }

    @Test
    void auditorPuedeVerPeroNoRegistrar() {
        Api.Sesion auditor = Api.usuario("AUDITOR");
        auditor.req().body(datos("No debería")).post("/api/incidentes").then().statusCode(403);
        auditor.req().get("/api/incidentes").then().statusCode(200);
    }

    @Test
    void listarFiltraPorTextoYEstado() {
        Api.Sesion a = Api.usuario("ANALISTA");
        String unico = "Phishing-" + System.nanoTime();
        long id = crear(a, unico);
        a.req().queryParam("q", unico.toLowerCase()).get("/api/incidentes").then().statusCode(200)
                .body("total", is(1)).body("items[0].id", is((int) id));
        a.req().queryParam("q", unico).queryParam("estado", "APROBADO").get("/api/incidentes")
                .then().body("total", is(0));
    }

    @Test
    void incidenteInexistenteDa404() {
        Api.admin().req().get("/api/incidentes/999999").then().statusCode(404);
    }
}
