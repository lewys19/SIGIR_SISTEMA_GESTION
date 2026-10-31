package pe.sigir.auth;

public record TokenResponse(String token, String tipo, long expiraEnSegundos) {
}
