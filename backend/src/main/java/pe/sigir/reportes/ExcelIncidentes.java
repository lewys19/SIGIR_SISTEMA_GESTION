package pe.sigir.reportes;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pe.sigir.incidentes.Incidente;
import pe.sigir.incidentes.Severidad;
import pe.sigir.reportes.ReporteDtos.Resumen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Reporte Excel con Apache POI: hoja "Incidentes" (filtros, fila fija, colores por severidad)
 * y hoja "Resumen". POI guarda los textos como texto, nunca como fórmula: "=HYPERLINK(...)" no se ejecuta.
 */
public final class ExcelIncidentes {

    private static final String[] CABECERA = {"Código", "Título", "Tipo", "Severidad", "Estado", "Regulador", "Área",
            "Ocurrencia", "Detección", "Fecha límite", "Vencido", "Reportado por", "Impacto", "Medidas"};
    private static final int[] ANCHOS = {16, 40, 24, 11, 13, 12, 16, 12, 12, 13, 9, 24, 40, 40};
    private static final Map<Severidad, String> COLOR = new EnumMap<>(Map.of(
            Severidad.BAJA, "E5F2EA", Severidad.MEDIA, "FBF2D9", Severidad.ALTA, "FDE7D2", Severidad.CRITICA, "FADBD8"));

    private ExcelIncidentes() {
    }

    public static byte[] generar(List<Incidente> incidentes, Resumen resumen, LocalDate desde, LocalDate hasta,
                                 LocalDate hoy) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos e = new Estilos(libro);
            hojaIncidentes(libro, e, incidentes, hoy);
            hojaResumen(libro, e, resumen, desde, hasta);
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void hojaIncidentes(XSSFWorkbook libro, Estilos e, List<Incidente> incidentes, LocalDate hoy) {
        Sheet hoja = libro.createSheet("Incidentes");
        Row cab = hoja.createRow(0);
        for (int c = 0; c < CABECERA.length; c++) {
            celda(cab, c, CABECERA[c], e.cabecera);
            hoja.setColumnWidth(c, ANCHOS[c] * 256);   // ancho fijo: autoSize necesita fuentes del sistema
        }
        int f = 1;
        for (Incidente i : incidentes) {
            Row r = hoja.createRow(f++);
            celda(r, 0, i.codigo, e.texto);
            celda(r, 1, i.titulo, e.texto);
            celda(r, 2, i.tipo.name(), e.texto);
            celda(r, 3, i.severidad.name(), e.severidad.get(i.severidad));
            celda(r, 4, i.estado.name(), e.texto);
            celda(r, 5, i.regulador.name(), e.texto);
            celda(r, 6, i.area, e.texto);
            fecha(r, 7, i.fechaOcurrencia, e.fecha);
            fecha(r, 8, i.fechaDeteccion, e.fecha);
            fecha(r, 9, i.fechaLimiteReporte, e.fecha);
            boolean vencido = i.estaVencido(hoy);
            celda(r, 10, vencido ? "Sí" : "No", vencido ? e.vencido : e.texto);
            celda(r, 11, i.reportadoPor.nombre, e.texto);
            celda(r, 12, i.impacto, e.texto);
            celda(r, 13, i.medidas, e.texto);
        }
        hoja.createFreezePane(0, 1);
        hoja.setAutoFilter(new CellRangeAddress(0, Math.max(f - 1, 1), 0, CABECERA.length - 1));
    }

    private static void hojaResumen(XSSFWorkbook libro, Estilos e, Resumen resumen, LocalDate desde, LocalDate hasta) {
        Sheet hoja = libro.createSheet("Resumen");
        hoja.setColumnWidth(0, 34 * 256);
        for (int c = 1; c < 4; c++) {
            hoja.setColumnWidth(c, 14 * 256);
        }
        int f = 0;
        celda(hoja.createRow(f++), 0, "SIGIR — Reporte de incidentes", e.titulo);
        celda(hoja.createRow(f++), 0, "Periodo (fecha de detección): " + desde + " a " + hasta, e.texto);
        f++;
        Row t = hoja.createRow(f++);
        celda(t, 0, "Total de incidentes", e.texto);
        numero(t, 1, resumen.total(), e.numero);
        Row a = hoja.createRow(f++);
        celda(a, 0, "Reportados a tiempo", e.texto);
        numero(a, 1, resumen.cumplimiento().aTiempo(), e.numero);
        Row fp = hoja.createRow(f++);
        celda(fp, 0, "Fuera de plazo", e.texto);
        numero(fp, 1, resumen.cumplimiento().fueraDePlazo(), e.numero);
        Row pc = hoja.createRow(f++);
        celda(pc, 0, "Cumplimiento de plazos (%)", e.texto);
        numero(pc, 1, resumen.cumplimiento().porcentajeATiempo(), e.numero);
        f++;
        Row cab = hoja.createRow(f++);
        String[] cols = {"Regulador", "Total", "Aprobados", "Vencidos"};
        for (int c = 0; c < cols.length; c++) {
            celda(cab, c, cols[c], e.cabecera);
        }
        for (ReporteDtos.PorRegulador r : resumen.porRegulador()) {
            Row fila = hoja.createRow(f++);
            celda(fila, 0, r.regulador(), e.texto);
            numero(fila, 1, r.total(), e.numero);
            numero(fila, 2, r.aprobados(), e.numero);
            numero(fila, 3, r.vencidos(), r.vencidos() > 0 ? e.vencido : e.numero);
        }
    }

    // ---------- celdas ----------

    private static void celda(Row r, int c, String valor, CellStyle estilo) {
        Cell cell = r.createCell(c);
        if (valor != null) {
            cell.setCellValue(valor);
        }
        cell.setCellStyle(estilo);
    }

    private static void fecha(Row r, int c, LocalDate valor, CellStyle estilo) {
        Cell cell = r.createCell(c);
        if (valor != null) {
            cell.setCellValue(valor);
        }
        cell.setCellStyle(estilo);
    }

    private static void numero(Row r, int c, double valor, CellStyle estilo) {
        Cell cell = r.createCell(c);
        cell.setCellValue(valor);
        cell.setCellStyle(estilo);
    }

    /** Estilos creados una sola vez por libro (Excel admite un número limitado). */
    private static final class Estilos {
        final CellStyle cabecera;
        final CellStyle titulo;
        final CellStyle texto;
        final CellStyle fecha;
        final CellStyle numero;
        final CellStyle vencido;
        final Map<Severidad, CellStyle> severidad = new EnumMap<>(Severidad.class);

        Estilos(XSSFWorkbook libro) {
            Font blanca = libro.createFont();
            blanca.setBold(true);
            blanca.setColor(IndexedColors.WHITE.getIndex());
            XSSFCellStyle c = libro.createCellStyle();
            c.setFillForegroundColor(color("0F3B3A"));
            c.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            c.setFont(blanca);
            c.setVerticalAlignment(VerticalAlignment.CENTER);
            cabecera = c;

            Font grande = libro.createFont();
            grande.setBold(true);
            grande.setFontHeightInPoints((short) 14);
            titulo = libro.createCellStyle();
            titulo.setFont(grande);

            texto = libro.createCellStyle();
            texto.setVerticalAlignment(VerticalAlignment.TOP);

            fecha = libro.createCellStyle();
            fecha.setDataFormat(libro.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));
            fecha.setVerticalAlignment(VerticalAlignment.TOP);

            numero = libro.createCellStyle();

            Font roja = libro.createFont();
            roja.setBold(true);
            roja.setColor(IndexedColors.DARK_RED.getIndex());
            vencido = libro.createCellStyle();
            vencido.setFont(roja);

            COLOR.forEach((sev, hex) -> {
                XSSFCellStyle s = libro.createCellStyle();
                s.setFillForegroundColor(color(hex));
                s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                s.setAlignment(HorizontalAlignment.CENTER);
                severidad.put(sev, s);
            });
        }

        private static XSSFColor color(String hex) {
            return new XSSFColor(java.util.HexFormat.of().parseHex(hex), null);
        }
    }
}
