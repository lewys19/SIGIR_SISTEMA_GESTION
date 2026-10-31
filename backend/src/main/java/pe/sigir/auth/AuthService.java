package pe.sigir.auth;

import jakarta.enterprise.context.ApplicationScoped;
import pe.sigir.auditoria.Auditor;
import pe.sigir.usuarios.Usuario;
import pe.sigir.usuarios.UsuarioDao;

import java.util.Optional;

/** Lógica de negocio del login. Solo conoce interfaces: se prueba sin base de datos. */
@ApplicationScoped
public class AuthService {

    private final UsuarioDao usuarios;
    private final PasswordHasher hasher;
    private final TokenEmisor emisor;
    private final Auditor auditor;

    public AuthService(UsuarioDao usuarios, PasswordHasher hasher, TokenEmisor emisor, Auditor auditor) {
        this.usuarios = usuarios;
        this.hasher = hasher;
        this.emisor = emisor;
        this.auditor = auditor;
    }

    public TokenResponse login(String email, String password) {
        String normalizado = email.trim().toLowerCase();
        Optional<Usuario> valido = usuarios.buscarPorEmail(normalizado)
                .filter(u -> u.activo)
                .filter(u -> hasher.coincide(password, u.passwordHash));
        if (valido.isEmpty()) {
            auditor.registrar(normalizado, "LOGIN_FALLIDO", "USUARIO", null, null);
            throw new CredencialesInvalidasException();
        }
        Usuario u = valido.get();
        auditor.registrar(u.email, "LOGIN_OK", "USUARIO", String.valueOf(u.id), null);
        return new TokenResponse(emisor.emitir(u), "Bearer", emisor.duracionSegundos());
    }
}
