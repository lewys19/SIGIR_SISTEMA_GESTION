package pe.sigir.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.sigir.usuarios.Rol;
import pe.sigir.usuarios.Usuario;
import pe.sigir.usuarios.UsuarioDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Test unitario puro: sin Quarkus ni base de datos, con dobles hechos a mano. */
class AuthServiceTest {

    private AuthService servicio;
    private final Map<String, Usuario> usuarios = new HashMap<>();
    private final List<String> auditado = new ArrayList<>();

    @BeforeEach
    void preparar() {
        UsuarioDao dao = email -> Optional.ofNullable(usuarios.get(email));
        PasswordHasher hasher = new PasswordHasher() {
            public String hashear(String plano) { return "hash:" + plano; }
            public boolean coincide(String plano, String hash) { return hash.equals("hash:" + plano); }
        };
        TokenEmisor emisor = u -> "token-de-" + u.email;
        servicio = new AuthService(dao, hasher, emisor,
                (actor, accion, entidad, id, detalle) -> auditado.add(accion + "|" + actor));

        usuarios.put("ana@sigir.pe", usuario("ana@sigir.pe", "Secreta1!", true));
        usuarios.put("baja@sigir.pe", usuario("baja@sigir.pe", "Secreta1!", false));
    }

    @Test
    void credencialesCorrectasDevuelvenTokenYSeAuditan() {
        TokenResponse r = servicio.login("ana@sigir.pe", "Secreta1!");
        assertEquals("token-de-ana@sigir.pe", r.token());
        assertEquals("Bearer", r.tipo());
        assertEquals(List.of("LOGIN_OK|ana@sigir.pe"), auditado);
    }

    @Test
    void passwordIncorrectaLanzaCredencialesInvalidasYSeAudita() {
        assertThrows(CredencialesInvalidasException.class, () -> servicio.login("ana@sigir.pe", "otra"));
        assertEquals(List.of("LOGIN_FALLIDO|ana@sigir.pe"), auditado);
    }

    @Test
    void usuarioInexistenteLanzaCredencialesInvalidas() {
        assertThrows(CredencialesInvalidasException.class, () -> servicio.login("nadie@sigir.pe", "Secreta1!"));
    }

    @Test
    void usuarioInactivoNoPuedeEntrar() {
        assertThrows(CredencialesInvalidasException.class, () -> servicio.login("baja@sigir.pe", "Secreta1!"));
    }

    @Test
    void emailSeComparaSinMayusculasNiEspacios() {
        assertNotNull(servicio.login("  ANA@sigir.pe ", "Secreta1!").token());
    }

    private Usuario usuario(String email, String password, boolean activo) {
        Usuario u = new Usuario();
        u.email = email;
        u.nombre = "Prueba";
        u.passwordHash = "hash:" + password;
        u.rol = Rol.ANALISTA;
        u.activo = activo;
        return u;
    }
}
