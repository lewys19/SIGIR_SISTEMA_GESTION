package pe.sigir.auth;

/** Mismo mensaje para "no existe", "contraseña mala" e "inactivo": no damos pistas a un atacante. */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos");
    }
}
