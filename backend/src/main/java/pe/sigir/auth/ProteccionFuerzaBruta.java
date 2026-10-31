package pe.sigir.auth;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

/**
 * Cuenta los intentos fallidos por correo en una caché de Guava que se vacía sola.
 * Tras N fallos la cuenta queda bloqueada; el bloqueo expira solo, sin tareas programadas.
 */
@ApplicationScoped
public class ProteccionFuerzaBruta {

    private final int maxIntentos;
    private final Duration bloqueo;
    private final Cache<String, Integer> fallos;

    @Inject
    public ProteccionFuerzaBruta(@ConfigProperty(name = "sigir.login.max-intentos") int maxIntentos,
                                 @ConfigProperty(name = "sigir.login.bloqueo-minutos") long minutos) {
        this(maxIntentos, Duration.ofMinutes(minutos), Ticker.systemTicker());
    }

    ProteccionFuerzaBruta(int maxIntentos, Duration bloqueo, Ticker reloj) {
        this.maxIntentos = maxIntentos;
        this.bloqueo = bloqueo;
        this.fallos = CacheBuilder.newBuilder()
                .expireAfterWrite(bloqueo)   // la ventana se renueva con cada fallo
                .maximumSize(100_000)        // tope de memoria ante ataques con miles de correos
                .ticker(reloj)
                .build();
    }

    public boolean estaBloqueado(String email) {
        Integer n = fallos.getIfPresent(email);
        return n != null && n >= maxIntentos;
    }

    public void registrarFallo(String email) {
        fallos.asMap().merge(email, 1, Integer::sum);
    }

    public void reiniciar(String email) {
        fallos.invalidate(email);
    }

    public long minutosDeBloqueo() {
        return bloqueo.toMinutes();
    }
}
