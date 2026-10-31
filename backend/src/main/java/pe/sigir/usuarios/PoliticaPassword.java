package pe.sigir.usuarios;

import pe.sigir.compartido.PeticionInvalidaException;

/** Mínimo 8 caracteres, con al menos una letra y un número. */
public final class PoliticaPassword {

    private PoliticaPassword() {
    }

    public static void validar(String password) {
        if (password == null || password.length() < 8 || password.length() > 72
                || !password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw new PeticionInvalidaException("La contraseña debe tener entre 8 y 72 caracteres, con letras y números");
        }
    }
}
