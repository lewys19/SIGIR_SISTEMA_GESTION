package pe.sigir.soporte;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

/** Utilidades para los tests de integración: login y creación de usuarios por rol. */
public final class Api {

    public static final String ADMIN = "admin@sigir.pe";
    public static final String CLAVE_ADMIN = "Admin123!";
    public static final String CLAVE = "Prueba123";

    private Api() {
    }

    public record Sesion(long id, String email, String token) {
        public RequestSpecification req() {
            return given().contentType(ContentType.JSON).header("Authorization", "Bearer " + token);
        }
    }

    public static String token(String email, String password) {
        return given().contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .post("/api/auth/login").then().statusCode(200)
                .extract().path("token");
    }

    public static Sesion admin() {
        return new Sesion(1, ADMIN, token(ADMIN, CLAVE_ADMIN));
    }

    /** Crea un usuario nuevo con el rol indicado y devuelve su sesión iniciada. */
    public static Sesion usuario(String rol) {
        String email = rol.toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8) + "@sigir.pe";
        Number id = admin().req()
                .body(Map.of("email", email, "nombre", "Usuario " + rol, "rol", rol, "password", CLAVE))
                .post("/api/usuarios").then().statusCode(201)
                .extract().path("id");
        return new Sesion(id.longValue(), email, token(email, CLAVE));
    }
}
