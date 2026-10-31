package pe.sigir.notificaciones;

import jakarta.enterprise.context.ApplicationScoped;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * El navegador no puede mandar la cabecera Authorization al abrir un WebSocket.
 * Por eso pide (con su JWT) un ticket de un solo uso que vence en 30 segundos y lo pasa en la URL.
 */
@ApplicationScoped
public class TicketsWs {

    public record Ticket(Long usuarioId, String rol, Instant vence) {
    }

    private static final SecureRandom AZAR = new SecureRandom();
    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();

    public String emitir(Long usuarioId, String rol) {
        Instant ahora = Instant.now();
        tickets.values().removeIf(t -> t.vence().isBefore(ahora));
        byte[] bytes = new byte[32];
        AZAR.nextBytes(bytes);
        String valor = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tickets.put(valor, new Ticket(usuarioId, rol, ahora.plusSeconds(30)));
        return valor;
    }

    public Optional<Ticket> consumir(String valor) {
        if (valor == null) {
            return Optional.empty();
        }
        Ticket t = tickets.remove(valor);
        return t != null && t.vence().isAfter(Instant.now()) ? Optional.of(t) : Optional.empty();
    }
}
