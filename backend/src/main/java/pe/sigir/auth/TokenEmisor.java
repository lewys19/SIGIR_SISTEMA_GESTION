package pe.sigir.auth;

import pe.sigir.usuarios.Usuario;

public interface TokenEmisor {
    String emitir(Usuario usuario);

    default long duracionSegundos() {
        return 3600;
    }
}
