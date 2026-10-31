package pe.sigir.reportes;

import pe.sigir.incidentes.Incidente;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** CSV para Excel en español: separador ";", UTF-8 con BOM y celdas protegidas contra inyección de fórmulas. */
public final class CsvIncidentes {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] CABECERA = {"Código", "Título", "Tipo", "Severidad", "Estado", "Regulador", "Área",
            "Fecha ocurrencia", "Fecha detección", "Fecha límite", "Vencido", "Reportado por", "Impacto", "Medidas"};

    private CsvIncidentes() {
    }

    public static String generar(List<Incidente> incidentes, LocalDate hoy) {
        StringBuilder sb = new StringBuilder("﻿");
        fila(sb, CABECERA);
        for (Incidente i : incidentes) {
            fila(sb, i.codigo, i.titulo, i.tipo.name(), i.severidad.name(), i.estado.name(), i.regulador.name(),
                    i.area, FECHA.format(i.fechaOcurrencia), FECHA.format(i.fechaDeteccion),
                    FECHA.format(i.fechaLimiteReporte), i.estaVencido(hoy) ? "Sí" : "No",
                    i.reportadoPor.nombre, i.impacto, i.medidas);
        }
        return sb.toString();
    }

    private static void fila(StringBuilder sb, String... celdas) {
        for (int c = 0; c < celdas.length; c++) {
            if (c > 0) {
                sb.append(';');
            }
            sb.append(celda(celdas[c]));
        }
        sb.append("\r\n");
    }

    static String celda(String valor) {
        if (valor == null) {
            return "";
        }
        String v = valor;
        if (!v.isEmpty() && "=+-@\t\r".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;   // Excel no la ejecutará como fórmula
        }
        if (v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
