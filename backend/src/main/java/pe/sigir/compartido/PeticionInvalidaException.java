package pe.sigir.compartido;

/** Datos de entrada inválidos que no cubre Bean Validation (se responde 400). */
public class PeticionInvalidaException extends RuntimeException {
    public PeticionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
