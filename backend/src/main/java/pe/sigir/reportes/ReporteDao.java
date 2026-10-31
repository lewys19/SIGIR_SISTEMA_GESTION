package pe.sigir.reportes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import pe.sigir.incidentes.Incidente;
import pe.sigir.reportes.ReporteDtos.*;

import java.time.LocalDate;
import java.util.List;

/** Consultas agregadas por rango de fecha de detección. */
@ApplicationScoped
public class ReporteDao {

    private final EntityManager em;

    public ReporteDao(EntityManager em) {
        this.em = em;
    }

    public record Rango(LocalDate desde, LocalDate hasta) {
        String sql(String alias) {
            return " WHERE " + alias + ".fecha_deteccion >= ?1 AND " + alias + ".fecha_deteccion <= ?2 ";
        }

        Query aplicar(Query q) {
            return q.setParameter(1, desde).setParameter(2, hasta);
        }
    }

    public long total(Rango r) {
        return n(r.aplicar(em.createNativeQuery("SELECT count(*) FROM incidentes i" + r.sql("i"))).getSingleResult());
    }

    @SuppressWarnings("unchecked")
    public List<PorRegulador> porRegulador(Rango r, LocalDate hoy) {
        List<Object[]> filas = r.aplicar(em.createNativeQuery("""
                SELECT regulador, count(*),
                       count(*) FILTER (WHERE estado IN ('APROBADO','CERRADO')),
                       count(*) FILTER (WHERE estado NOT IN ('APROBADO','CERRADO') AND fecha_limite_reporte < ?3)
                FROM incidentes i""" + r.sql("i") + " GROUP BY regulador ORDER BY count(*) DESC"))
                .setParameter(3, hoy).getResultList();
        return filas.stream().map(f -> new PorRegulador((String) f[0], n(f[1]), n(f[2]), n(f[3]))).toList();
    }

    @SuppressWarnings("unchecked")
    public List<PorTipo> porTipo(Rango r) {
        List<Object[]> filas = r.aplicar(em.createNativeQuery(
                "SELECT tipo, count(*) FROM incidentes i" + r.sql("i") + " GROUP BY tipo ORDER BY count(*) DESC"))
                .getResultList();
        return filas.stream().map(f -> new PorTipo((String) f[0], n(f[1]))).toList();
    }

    /** ¿Se aprobó (reportó) dentro del plazo regulatorio? Usa la primera aprobación de cada incidente. */
    public Cumplimiento cumplimiento(Rango r) {
        Object[] f = (Object[]) r.aplicar(em.createNativeQuery("""
                SELECT count(*) FILTER (WHERE CAST(a.fecha AS date) <= i.fecha_limite_reporte),
                       count(*) FILTER (WHERE CAST(a.fecha AS date) >  i.fecha_limite_reporte),
                       avg(extract(epoch FROM a.fecha - i.creado_en) / 86400)
                FROM incidentes i
                JOIN (SELECT incidente_id, min(fecha) AS fecha FROM revisiones
                      WHERE decision = 'APROBADO' GROUP BY incidente_id) a ON a.incidente_id = i.id""" + r.sql("i")))
                .getSingleResult();
        long aTiempo = n(f[0]);
        long fuera = n(f[1]);
        double pct = aTiempo + fuera == 0 ? 0 : Math.round(aTiempo * 1000.0 / (aTiempo + fuera)) / 10.0;
        Double prom = f[2] == null ? null : Math.round(((Number) f[2]).doubleValue() * 10) / 10.0;
        return new Cumplimiento(aTiempo, fuera, pct, prom);
    }

    public List<Incidente> incidentes(Rango r) {
        return em.createQuery("""
                        select i from Incidente i
                        where i.fechaDeteccion >= :desde and i.fechaDeteccion <= :hasta
                        order by i.fechaDeteccion desc, i.id desc""", Incidente.class)
                .setParameter("desde", r.desde()).setParameter("hasta", r.hasta()).getResultList();
    }

    private static long n(Object o) {
        return ((Number) o).longValue();
    }
}
