package pe.sigir.usuarios;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import pe.sigir.soporte.Api;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class UsuariosResourceTest {

    @Test
    void crearUsuarioYQuePuedaEntrar() {
        Api.Sesion nuevo = Api.usuario("ANALISTA");
        nuevo.req().get("/api/auth/me").then().statusCode(200)
                .body("rol", is("ANALISTA"))
                .body("permisos", hasItem("incidentes:crear"))
                .body("permisos", not(hasItem("usuarios:gestionar")));
    }

    @Test
    void emailDuplicadoDa409() {
        Api.admin().req()
                .body(Map.of("email", Api.ADMIN, "nombre", "Otro", "rol", "ANALISTA", "password", Api.CLAVE))
                .post("/api/usuarios").then().statusCode(409);
    }

    @Test
    void passwordDebilDa400() {
        Api.admin().req()
                .body(Map.of("email", "debil@sigir.pe", "nombre", "Débil", "rol", "ANALISTA", "password", "123"))
                .post("/api/usuarios").then().statusCode(400);
    }

    @Test
    void usuarioDesactivadoNoPuedeEntrar() {
        Api.Sesion u = Api.usuario("ANALISTA");
        Api.admin().req().body(Map.of("nombre", "Dado de baja", "rol", "ANALISTA", "activo", false))
                .put("/api/usuarios/" + u.id()).then().statusCode(200).body("activo", is(false));

        given().contentType(ContentType.JSON).body(Map.of("email", u.email(), "password", Api.CLAVE))
                .post("/api/auth/login").then().statusCode(401);
    }

    @Test
    void adminNoPuedeDesactivarseASiMismo() {
        Api.admin().req().body(Map.of("nombre", "Administrador", "rol", "ADMIN", "activo", false))
                .put("/api/usuarios/1").then().statusCode(409);
    }

    @Test
    void resetearPasswordCambiaLaClave() {
        Api.Sesion u = Api.usuario("ANALISTA");
        Api.admin().req().body(Map.of("password", "NuevaClave9")).put("/api/usuarios/" + u.id() + "/password")
                .then().statusCode(204);
        Api.token(u.email(), "NuevaClave9");
    }

    @Test
    void cambiarMiPasswordExigeLaActual() {
        Api.Sesion u = Api.usuario("ANALISTA");
        u.req().body(Map.of("actual", "incorrecta", "nueva", "OtraClave77")).put("/api/auth/password")
                .then().statusCode(401);
        u.req().body(Map.of("actual", Api.CLAVE, "nueva", "OtraClave77")).put("/api/auth/password")
                .then().statusCode(204);
        Api.token(u.email(), "OtraClave77");
    }

    @Test
    void analistaNoPuedeGestionarUsuarios() {
        Api.usuario("ANALISTA").req().get("/api/usuarios").then().statusCode(403);
    }

    @Test
    void usuarioInexistenteDa404() {
        Api.admin().req().body(Map.of("nombre", "X", "rol", "ANALISTA", "activo", true))
                .put("/api/usuarios/999999").then().statusCode(404);
    }
}
