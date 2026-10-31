package pe.sigir.incidentes;

import jakarta.persistence.*;
import pe.sigir.compartido.NegocioException;
import pe.sigir.usuarios.Usuario;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "incidentes")
public class Incidente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, updatable = false)
    public String codigo;

    @Column(nullable = false)
    public String titulo;

    @Column(nullable = false)
    public String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TipoIncidente tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Severidad severidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public EstadoIncidente estado = EstadoIncidente.BORRADOR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Regulador regulador;

    public String area;

    @Column(name = "fecha_ocurrencia", nullable = false)
    public LocalDate fechaOcurrencia;

    @Column(name = "fecha_deteccion", nullable = false)
    public LocalDate fechaDeteccion;

    @Column(name = "fecha_limite_reporte", nullable = false)
    public LocalDate fechaLimiteReporte;

    public String impacto;
    public String medidas;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "reportado_por_id", updatable = false)
    public Usuario reportadoPor;

    @Column(name = "creado_en", nullable = false, insertable = false, updatable = false)
    public OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false, insertable = false)
    public OffsetDateTime actualizadoEn;

    @Version
    public long version;

    private static final Set<EstadoIncidente> EDITABLES = EnumSet.of(EstadoIncidente.BORRADOR, EstadoIncidente.RECHAZADO);
    private static final Set<EstadoIncidente> FINALIZADOS = EnumSet.of(EstadoIncidente.APROBADO, EstadoIncidente.CERRADO);

    // ---------- máquina de estados ----------

    public boolean esEditable() {
        return EDITABLES.contains(estado);
    }

    public void enviarARevision() {
        exigir(esEditable(), "Solo se envían a revisión borradores o incidentes rechazados");
        estado = EstadoIncidente.EN_REVISION;
    }

    public void aprobar() {
        exigir(estado == EstadoIncidente.EN_REVISION, "Solo se aprueban incidentes en revisión");
        estado = EstadoIncidente.APROBADO;
    }

    public void rechazar() {
        exigir(estado == EstadoIncidente.EN_REVISION, "Solo se rechazan incidentes en revisión");
        estado = EstadoIncidente.RECHAZADO;
    }

    public void cerrar() {
        exigir(estado == EstadoIncidente.APROBADO, "Solo se cierran incidentes aprobados");
        estado = EstadoIncidente.CERRADO;
    }

    /** Pasó la fecha límite y todavía no fue aprobado (reportado). */
    public boolean estaVencido(LocalDate hoy) {
        return !FINALIZADOS.contains(estado) && fechaLimiteReporte != null && fechaLimiteReporte.isBefore(hoy);
    }

    @PreUpdate
    void alActualizar() {
        actualizadoEn = OffsetDateTime.now();
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new NegocioException(mensaje);
        }
    }
}
