package pe.sigir.auth;

import java.util.Set;

public record UsuarioActualResponse(String id, String email, String nombre, String rol, Set<String> permisos) {
}
