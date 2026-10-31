package pe.sigir.auth;

public class DemasiadosIntentosException extends RuntimeException {

    private final long minutos;

    public DemasiadosIntentosException(long minutos) {
        super("Demasiados intentos fallidos. Vuelve a intentarlo en " + minutos + " minutos");
        this.minutos = minutos;
    }

    public long minutos() {
        return minutos;
    }
}
