package pe.sigir.compartido;

/** Regla de negocio violada (se responde 409 Conflict). */
public class NegocioException extends RuntimeException {
    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
