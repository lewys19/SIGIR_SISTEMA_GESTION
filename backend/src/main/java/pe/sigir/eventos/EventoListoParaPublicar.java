package pe.sigir.eventos;

import java.util.Set;

public record EventoListoParaPublicar(EventoIncidente evento, Set<Long> destinatarios, String titulo) {
}
