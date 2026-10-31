package pe.sigir.auth;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/** Test de integración: Quarkus real + PostgreSQL de prueba (Dev Services, en Docker). */
@QuarkusTest
class AuthResourceTest {

    private static final String ADMIN = "admin@sigir.pe";
    private static final String CLAVE = "Admin123!";

    private String login(String email, String password) {
        return given().contentType(ContentType.JSON)
                .body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
                .when().post("/api/auth/login")
                .then().statusCode(200)
                .extract().path("token");
    }

    @Test
    void loginCorrectoDevuelveJwt() {
        given().contentType(ContentType.JSON)
                .body("{\"email\":\"" + ADMIN + "\",\"password\":\"" + CLAVE + "\"}")
                .when().post("/api/auth/login")
                .then().statusCode(200)
                .body("token", not(emptyOrNullString()))
                .body("tipo", is("Bearer"))
                .body("expiraEnSegundos", greaterThan(0));
    }

    @Test
    void passwordIncorrectaDa401SinDecirQueFallo() {
        given().contentType(ContentType.JSON)
                .body("{\"email\":\"" + ADMIN + "\",\"password\":\"mala\"}")
                .when().post("/api/auth/login")
                .then().statusCode(401)
                .body("mensaje", is("Correo o contraseña incorrectos"));
    }

    @Test
    void fuerzaBrutaBloqueaLaCuentaCon429() {
        pe.sigir.soporte.Api.Sesion victima = pe.sigir.soporte.Api.usuario("ANALISTA");
        for (int i = 0; i < 5; i++) {
            given().contentType(ContentType.JSON)
                    .body("{\"email\":\"" + victima.email() + "\",\"password\":\"Adivina" + i + "\"}")
                    .when().post("/api/auth/login").then().statusCode(401);
        }
        given().contentType(ContentType.JSON)
                .body("{\"email\":\"" + victima.email() + "\",\"password\":\"" + pe.sigir.soporte.Api.CLAVE + "\"}")
                .when().post("/api/auth/login")
                .then().statusCode(429)
                .header("Retry-After", "900")
                .body("mensaje", containsString("15 minutos"));
    }

    @Test
    void emailMalFormadoDa400() {
        given().contentType(ContentType.JSON)
                .body("{\"email\":\"no-es-correo\",\"password\":\"x\"}")
                .when().post("/api/auth/login")
                .then().statusCode(400);
    }

    @Test
    void meSinTokenDa401() {
        given().when().get("/api/auth/me").then().statusCode(401);
    }

    @Test
    void meConTokenFalsoDa401() {
        given().header("Authorization", "Bearer inventado.no.vale")
                .when().get("/api/auth/me").then().statusCode(401);
    }

    @Test
    void meConTokenDevuelveUsuarioYRol() {
        String token = login(ADMIN, CLAVE);
        given().header("Authorization", "Bearer " + token)
                .when().get("/api/auth/me")
                .then().statusCode(200)
                .body("email", is(ADMIN))
                .body("rol", is("ADMIN"))
                .body("$", not(hasKey("passwordHash")));
    }
}
