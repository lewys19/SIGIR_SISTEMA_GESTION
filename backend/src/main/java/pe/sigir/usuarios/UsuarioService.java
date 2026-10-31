package pe.sigir.usuarios;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import pe.sigir.auditoria.Auditor;
import pe.sigir.auth.PasswordHasher;
import pe.sigir.compartido.NegocioException;
import pe.sigir.compartido.NoEncontradoException;
import pe.sigir.compartido.Textos;
import pe.sigir.compartido.UsuarioActual;
import pe.sigir.usuarios.UsuarioDtos.ActualizarUsuario;
import pe.sigir.usuarios.UsuarioDtos.CrearUsuario;

import java.util.List;

@ApplicationScoped
public class UsuarioService {

    private final UsuarioDao dao;
    private final PasswordHasher hasher;
    private final Auditor auditor;
    private final UsuarioActual actual;

    public UsuarioService(UsuarioDao dao, PasswordHasher hasher, Auditor auditor, UsuarioActual actual) {
        this.dao = dao;
        this.hasher = hasher;
        this.auditor = auditor;
        this.actual = actual;
    }

    public List<Usuario> listar() {
        return dao.listarTodos();
    }

    @Transactional
    public Usuario crear(CrearUsuario p) {
        String email = Textos.email(p.email());
        if (dao.buscarPorEmail(email).isPresent()) {
            throw new NegocioException("Ya existe un usuario con ese correo");
        }
        PoliticaPassword.validar(p.password());
        Usuario u = new Usuario();
        u.email = email;
        u.nombre = Textos.limpiar(p.nombre());
        u.rol = p.rol();
        u.passwordHash = hasher.hashear(p.password());
        dao.guardar(u);
        auditor.registrar(actual.email(), "USUARIO_CREADO", "USUARIO", String.valueOf(u.id),
                u.email + " rol=" + u.rol);
        return u;
    }

    @Transactional
    public Usuario actualizar(Long id, ActualizarUsuario p) {
        Usuario u = obtener(id);
        boolean esElMismo = u.id.equals(actual.id());
        if (esElMismo && (!p.activo() || p.rol() != u.rol)) {
            throw new NegocioException("No puedes desactivarte ni cambiarte el rol a ti mismo");
        }
        String antes = "rol=" + u.rol + " activo=" + u.activo;
        u.nombre = Textos.limpiar(p.nombre());
        u.rol = p.rol();
        u.activo = p.activo();
        auditor.registrar(actual.email(), "USUARIO_ACTUALIZADO", "USUARIO", String.valueOf(u.id),
                u.email + " " + antes + " -> rol=" + u.rol + " activo=" + u.activo);
        return u;
    }

    @Transactional
    public void resetearPassword(Long id, String nueva) {
        Usuario u = obtener(id);
        PoliticaPassword.validar(nueva);
        u.passwordHash = hasher.hashear(nueva);
        auditor.registrar(actual.email(), "PASSWORD_RESETEADA", "USUARIO", String.valueOf(u.id), u.email);
    }

    @Transactional
    public void cambiarMiPassword(String actualPlano, String nueva) {
        Usuario u = obtener(actual.id());
        if (!hasher.coincide(actualPlano, u.passwordHash)) {
            throw new pe.sigir.auth.CredencialesInvalidasException();
        }
        PoliticaPassword.validar(nueva);
        u.passwordHash = hasher.hashear(nueva);
        auditor.registrar(u.email, "PASSWORD_CAMBIADA", "USUARIO", String.valueOf(u.id), null);
    }

    private Usuario obtener(Long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new NoEncontradoException("Usuario no encontrado"));
    }
}
