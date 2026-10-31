package pe.sigir.usuarios;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public final class UsuarioDtos {

    private UsuarioDtos() {
    }

    public record CrearUsuario(@NotBlank @Email @Size(max = 160) String email,
                               @NotBlank @Size(max = 120) String nombre,
                               @NotNull Rol rol,
                               @NotBlank String password) {
    }

    public record ActualizarUsuario(@NotBlank @Size(max = 120) String nombre,
                                    @NotNull Rol rol,
                                    boolean activo) {
    }

    public record NuevaPassword(@NotBlank String password) {
    }

    public record UsuarioResponse(Long id, String email, String nombre, Rol rol, boolean activo,
                                  OffsetDateTime creadoEn) {
        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.id, u.email, u.nombre, u.rol, u.activo, u.creadoEn);
        }
    }
}
