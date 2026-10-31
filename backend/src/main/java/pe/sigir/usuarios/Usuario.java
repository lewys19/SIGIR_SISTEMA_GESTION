package pe.sigir.usuarios;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, length = 160)
    public String email;

    @Column(nullable = false, length = 120)
    public String nombre;

    @Column(name = "password_hash", nullable = false, length = 100)
    public String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public Rol rol;

    @Column(nullable = false)
    public boolean activo = true;

    @Column(name = "creado_en", nullable = false, insertable = false, updatable = false)
    public OffsetDateTime creadoEn;
}
