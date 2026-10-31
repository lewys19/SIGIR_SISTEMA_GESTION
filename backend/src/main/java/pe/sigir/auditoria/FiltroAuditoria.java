package pe.sigir.auditoria;

import java.time.LocalDate;

public record FiltroAuditoria(String accion, String actor, LocalDate desde, LocalDate hasta, int pagina, int tamano) {
}
