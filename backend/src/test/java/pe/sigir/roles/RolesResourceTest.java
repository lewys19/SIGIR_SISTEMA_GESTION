package pe.sigir.roles;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import pe.sigir.soporte.Api;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@QuarkusTest
class RolesResourceTest {

    @Test
    void adminVeLaMatrizYElCatalogo() {
        Api.admin().req().get("/api/roles").then().statusCode(200)
                .body("catalogo", hasItems("incidentes:crear", "auditoria:ver"))
                .body("roles.find { it.rol == 'ADMIN' }.permisos", hasItem("roles:gestionar"));
    }

    @Test
    void analistaNoPuedeVerLaMatriz() {
        Api.usuario("ANALISTA").req().get("/api/roles").then().statusCode(403);
    }

    @Test
    void cambiarPermisosSeAplicaAlInstanteSinNuevoLogin() {
        Api.Sesion auditor = Api.usuario("AUDITOR");
        auditor.req().get("/api/usuarios").then().statusCode(403);

        List<String> original = Api.admin().req().get("/api/roles").then().extract()
                .path("roles.find { it.rol == 'AUDITOR' }.permisos");
        try {
            List<String> ampliado = new java.util.ArrayList<>(original);
            ampliado.add("usuarios:gestionar");
            Api.admin().req().body(Map.of("permisos", ampliado)).put("/api/roles/AUDITOR").then().statusCode(200);

            auditor.req().get("/api/usuarios").then().statusCode(200);
            auditor.req().get("/api/auth/me").then().body("permisos", hasItem("usuarios:gestionar"));
        } finally {
            Api.admin().req().body(Map.of("permisos", original)).put("/api/roles/AUDITOR").then().statusCode(200);
        }
        auditor.req().get("/api/usuarios").then().statusCode(403);
    }

    @Test
    void adminNoPuedeQuitarseLaGestionDeRoles() {
        Api.admin().req().body(Map.of("permisos", List.of("dashboard:ver"))).put("/api/roles/ADMIN")
                .then().statusCode(409);
    }

    @Test
    void permisoInexistenteDa400() {
        Api.admin().req().body(Map.of("permisos", List.of("hackear:todo"))).put("/api/roles/ANALISTA")
                .then().statusCode(400);
    }
}
