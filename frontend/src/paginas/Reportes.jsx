import { useState } from 'react'
import { api, descargar, query } from '../api/cliente'
import { Campo, Cargando, MensajeError, useCarga } from '../componentes/ui'
import { etiqueta, hoyIso } from '../lib/formato'

const haceDias = (n) => {
  const d = new Date()
  d.setDate(d.getDate() - n)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

export default function Reportes() {
  const [rango, setRango] = useState({ desde: haceDias(90), hasta: hoyIso() })
  const [errorDescarga, setErrorDescarga] = useState(null)
  const { datos, error, cargando } = useCarga(() => api(`/reportes/resumen${query(rango)}`), [rango])

  const exportar = async (formato) => {
    setErrorDescarga(null)
    try { await descargar(`/reportes/incidentes.${formato}${query(rango)}`, `incidentes.${formato}`) } catch (e) { setErrorDescarga(e) }
  }
  const maxTipo = Math.max(1, ...(datos?.porTipo ?? []).map((t) => t.total))

  return (
    <div className="pila">
      <div className="titulo-con-accion">
        <h1>Reportes</h1>
        <div className="acciones">
          <button className="btn-secundario" onClick={() => exportar('csv')}>⬇ CSV</button>
          <button className="btn-principal" onClick={() => exportar('xlsx')}>⬇ Exportar Excel</button>
        </div>
      </div>
      <div className="filtros tarjeta">
        <Campo etiqueta="Detectados desde">
          <input type="date" value={rango.desde} max={rango.hasta} onChange={(e) => setRango({ ...rango, desde: e.target.value })} />
        </Campo>
        <Campo etiqueta="Hasta">
          <input type="date" value={rango.hasta} min={rango.desde} onChange={(e) => setRango({ ...rango, hasta: e.target.value })} />
        </Campo>
      </div>
      <MensajeError error={error ?? errorDescarga} />
      {cargando ? <Cargando /> : datos && (
        <>
          <div className="kpis">
            <div className="kpi"><span>Incidentes en el periodo</span><strong>{datos.total}</strong></div>
            <div className="kpi"><span>Reportados a tiempo</span><strong>{datos.cumplimiento.aTiempo}</strong></div>
            <div className="kpi peligro"><span>Fuera de plazo</span><strong>{datos.cumplimiento.fueraDePlazo}</strong></div>
            <div className="kpi"><span>Cumplimiento de plazos</span><strong>{datos.cumplimiento.porcentajeATiempo}%</strong></div>
            <div className="kpi"><span>Días promedio hasta aprobación</span><strong>{datos.cumplimiento.promedioDiasAprobacion ?? '—'}</strong></div>
          </div>
          <div className="rejilla-2">
            <section className="tarjeta sin-relleno">
              <h2 className="con-relleno">Por regulador</h2>
              <table className="tabla">
                <thead><tr><th>Regulador</th><th>Total</th><th>Aprobados</th><th>Vencidos</th></tr></thead>
                <tbody>
                  {datos.porRegulador.length === 0 && <tr><td colSpan="4" className="nota">Sin datos en el periodo.</td></tr>}
                  {datos.porRegulador.map((r) => (
                    <tr key={r.regulador}>
                      <td>{r.regulador}</td><td>{r.total}</td><td>{r.aprobados}</td>
                      <td className={r.vencidos ? 'texto-peligro' : ''}>{r.vencidos}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>
            <section className="tarjeta">
              <h2>Por tipo</h2>
              <ul className="barras">
                {datos.porTipo.map((t) => (
                  <li key={t.tipo}>
                    <span>{etiqueta(t.tipo)}</span>
                    <div className="barra-fondo"><div className="barra-llena" style={{ width: `${(t.total * 100) / maxTipo}%` }} /></div>
                    <strong>{t.total}</strong>
                  </li>
                ))}
              </ul>
            </section>
          </div>
        </>
      )}
    </div>
  )
}
