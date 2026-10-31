package pe.sigir.dashboard;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import pe.sigir.dashboard.DashboardDtos.Kpis;
import pe.sigir.dashboard.DashboardDtos.PuntoMes;
import pe.sigir.incidentes.EstadoIncidente;
import pe.sigir.incidentes.Incidente;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class DashboardDaoJpa implements DashboardDao {

    private static final Set<String> COLUMNAS = Set.of("severidad", "estado");

    private final EntityManager em;

    public DashboardDaoJpa(EntityManager em) {
        this.em = em;
    }

    @Override
    public Kpis kpis(LocalDate hoy) {
        Object[] f = (Object[]) em.createNativeQuery("""
                SELECT count(*),
                       count(*) FILTER (WHERE estado <> 'CERRADO'),
                       count(*) FILTER (WHERE estado = 'EN_REVISION'),
                       count(*) FILTER (WHERE severidad = 'CRITICA' AND estado <> 'CERRADO'),
                       count(*) FILTER (WHERE estado NOT IN ('APROBADO','CERRADO') AND fecha_limite_reporte < ?1)
                FROM incidentes""").setParameter(1, hoy).getSingleResult();
        return new Kpis(n(f[0]), n(f[1]), n(f[2]), n(f[3]), n(f[4]));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PuntoMes> porMes(LocalDate primerMes, int meses) {
        List<Object[]> filas = em.createNativeQuery("""
                SELECT to_char(m, 'YYYY-MM'),
                       (SELECT count(*) FROM incidentes i WHERE date_trunc('month', i.creado_en)::date = m::date),
                       (SELECT count(*) FROM revisiones r
                         WHERE r.decision = 'CERRADO' AND date_trunc('month', r.fecha)::date = m::date)
                FROM generate_series(CAST(?1 AS date), CAST(?1 AS date) + make_interval(months => ?2 - 1),
                                     interval '1 month') AS m
                ORDER BY m""")
                .setParameter(1, primerMes).setParameter(2, meses).getResultList();
        return filas.stream().map(f -> new PuntoMes((String) f[0], n(f[1]), n(f[2]))).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Long> conteoPor(String columna) {
        if (!COLUMNAS.contains(columna)) {
            throw new IllegalArgumentException(columna);
        }
        List<Object[]> filas = em.createNativeQuery(
                "SELECT " + columna + ", count(*) FROM incidentes GROUP BY " + columna).getResultList();
        Map<String, Long> m = new LinkedHashMap<>();
        filas.forEach(f -> m.put((String) f[0], n(f[1])));
        return m;
    }

    @Override
    public List<Incidente> urgentes(int limite) {
        return em.createQuery("""
                        select i from Incidente i
                        where i.estado not in (:fin)
                        order by i.fechaLimiteReporte asc, i.id asc""", Incidente.class)
                .setParameter("fin", List.of(EstadoIncidente.APROBADO, EstadoIncidente.CERRADO))
                .setMaxResults(limite).getResultList();
    }

    private static long n(Object o) {
        return ((Number) o).longValue();
    }
}
