package pe.sigir.incidentes;

import jakarta.validation.constraints.*;
import pe.sigir.revision.Revision;
import pe.sigir.usuarios.Usuario;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

public final class IncidenteDtos {

    private IncidenteDtos() {
    }

    public record IncidenteRequest(@NotBlank @Size(max = 200) String titulo,
                                   @NotBlank @Size(max = 5000) String descripcion,
                                   @NotNull TipoIncidente tipo,
                                   @NotNull Severidad severidad,
                                   @NotNull Regulador regulador,
                                   @Size(max = 80) String area,
                                   @NotNull @PastOrPresent LocalDate fechaOcurrencia,
                                   @NotNull @PastOrPresent LocalDate fechaDeteccion,
                                   @Size(max = 5000) String impacto,
                                   @Size(max = 5000) String medidas) {
    }

    public record UsuarioRef(Long id, String nombre, String email) {
        public static UsuarioRef de(Usuario u) {
            return new UsuarioRef(u.id, u.nombre, u.email);
        }
    }

    public record IncidenteResumen(Long id, String codigo, String titulo, TipoIncidente tipo, Severidad severidad,
                                   EstadoIncidente estado, Regulador regulador, LocalDate fechaLimiteReporte,
                                   boolean vencido, UsuarioRef reportadoPor, OffsetDateTime creadoEn) {
        public static IncidenteResumen de(Incidente i) {
            return new IncidenteResumen(i.id, i.codigo, i.titulo, i.tipo, i.severidad, i.estado, i.regulador,
                    i.fechaLimiteReporte, i.estaVencido(LocalDate.now()), UsuarioRef.de(i.reportadoPor), i.creadoEn);
        }
    }

    public record HistorialItem(String decision, String comentario, UsuarioRef usuario, OffsetDateTime fecha) {
        public static HistorialItem de(Revision r) {
            return new HistorialItem(r.decision.name(), r.comentario, UsuarioRef.de(r.revisor), r.fecha);
        }
    }

    public record IncidenteDetalle(Long id, String codigo, String titulo, String descripcion, TipoIncidente tipo,
                                   Severidad severidad, EstadoIncidente estado, Regulador regulador, String area,
                                   LocalDate fechaOcurrencia, LocalDate fechaDeteccion, LocalDate fechaLimiteReporte,
                                   boolean vencido, String impacto, String medidas, UsuarioRef reportadoPor,
                                   OffsetDateTime creadoEn, OffsetDateTime actualizadoEn,
                                   List<HistorialItem> historial, Set<String> acciones) {
    }

    public record Catalogos(TipoIncidente[] tipos, Severidad[] severidades, Regulador[] reguladores,
                            EstadoIncidente[] estados) {
    }
}
