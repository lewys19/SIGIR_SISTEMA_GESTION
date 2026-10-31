package pe.sigir.notificaciones;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    public Long usuarioId;

    @Column(nullable = false, updatable = false)
    public String tipo;

    @Column(nullable = false, updatable = false)
    public String titulo;

    @Column(updatable = false)
    public String mensaje;

    @Column(name = "incidente_id", updatable = false)
    public Long incidenteId;

    @Column(nullable = false)
    public boolean leida;

    @Column(name = "creada_en", nullable = false, insertable = false, updatable = false)
    public OffsetDateTime creadaEn;
}
