package pe.sigir.auth;

public interface PasswordHasher {
    String hashear(String plano);
    boolean coincide(String plano, String hash);
}
