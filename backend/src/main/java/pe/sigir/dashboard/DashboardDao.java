package pe.sigir.dashboard;

import pe.sigir.dashboard.DashboardDtos.Kpis;
import pe.sigir.dashboard.DashboardDtos.PuntoMes;
import pe.sigir.incidentes.Incidente;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DashboardDao {
    Kpis kpis(LocalDate hoy);

    List<PuntoMes> porMes(LocalDate primerMes, int meses);

    Map<String, Long> conteoPor(String columna);

    List<Incidente> urgentes(int limite);
}
