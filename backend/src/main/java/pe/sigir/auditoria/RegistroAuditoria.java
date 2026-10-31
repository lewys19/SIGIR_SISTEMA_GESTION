package pe.sigir.auditoria;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "auditoria")
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, insertable = false, updatable = false)
    public OffsetDateTime fecha;

    @Column(nullable = false, updatable = false)
    public String actor;

    @Column(nullable = false, updatable = false)
    public String accion;

    @Column(updatable = false)
    public String entidad;

    @Column(name = "entidad_id", updatable = false)
    public String entidadId;

    @Column(updatable = false)
    public String detalle;
}
