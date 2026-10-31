package pe.sigir.auth;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BcryptPasswordHasher implements PasswordHasher {

    private static final int COSTE = 12;

    @Override
    public String hashear(String plano) {
        return BcryptUtil.bcryptHash(plano, COSTE);
    }

    @Override
    public boolean coincide(String plano, String hash) {
        return BcryptUtil.matches(plano, hash);
    }
}
