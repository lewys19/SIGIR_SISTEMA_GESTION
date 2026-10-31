package pe.sigir.revision;

import jakarta.persistence.*;
import pe.sigir.usuarios.Usuario;

import java.time.OffsetDateTime;

/** Cada paso del flujo de aprobación queda en el historial del incidente. */
@Entity
@Table(name = "revisiones")
public class Revision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "incidente_id", nullable = false, updatable = false)
    public Long incidenteId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "revisor_id", updatable = false)
    public Usuario revisor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    public DecisionRevision decision;

    @Column(updatable = false)
    public String comentario;

    @Column(nullable = false, insertable = false, updatable = false)
    public OffsetDateTime fecha;
}
