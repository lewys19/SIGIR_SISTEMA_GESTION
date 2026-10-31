package pe.sigir.compartido;

/** Tiene el permiso general, pero no sobre este recurso concreto (se responde 403). */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
