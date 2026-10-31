package pe.sigir.auditoria;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import pe.sigir.soporte.Api;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class AuditoriaResourceTest {

    @Inject
    EntityManager em;

    @Test
    void loginFallidoQuedaRegistrado() {
        String email = "intruso-" + System.nanoTime() + "@sigir.pe";
        given().contentType(ContentType.JSON).body(Map.of("email", email, "password", "x"))
                .post("/api/auth/login").then().statusCode(401);

        Api.admin().req().queryParam("accion", "LOGIN_FALLIDO").queryParam("actor", email)
                .get("/api/auditoria").then().statusCode(200)
                .body("total", is(1))
                .body("items[0].actor", is(email));
    }

    @Test
    void crearUsuarioQuedaRegistradoConElActor() {
        Api.Sesion u = Api.usuario("ANALISTA");
        Api.admin().req().queryParam("accion", "USUARIO_CREADO").queryParam("tamano", 100)
                .get("/api/auditoria").then().statusCode(200)
                .body("items.findAll { it.detalle.contains('" + u.email() + "') }.actor", hasItem(Api.ADMIN));
    }

    @Test
    void analistaNoVeLaAuditoria() {
        Api.usuario("ANALISTA").req().get("/api/auditoria").then().statusCode(403);
    }

    @Test
    void nadiePuedeModificarNiBorrarLaAuditoria() {
        assertThrows(Exception.class, () -> QuarkusTransaction.requiringNew().run(() ->
                em.createNativeQuery("UPDATE auditoria SET actor = 'yo'").executeUpdate()));
        assertThrows(Exception.class, () -> QuarkusTransaction.requiringNew().run(() ->
                em.createNativeQuery("DELETE FROM auditoria").executeUpdate()));
    }
}
