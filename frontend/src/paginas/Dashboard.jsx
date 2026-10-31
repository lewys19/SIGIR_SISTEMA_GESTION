import { Link } from 'react-router-dom'
import { CartesianGrid, Cell, Legend, Line, LineChart, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { api } from '../api/cliente'
import { useEventos } from '../eventos/EventosContext'
import { Cargando, Insignia, MensajeError, useCarga } from '../componentes/ui'
import { etiqueta, textoPlazo } from '../lib/formato'

const COLOR_SEVERIDAD = { BAJA: '#5b9a74', MEDIA: '#e0b23a', ALTA: '#f28c28', CRITICA: '#c0392b' }
const MESES = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Set', 'Oct', 'Nov', 'Dic']

function Kpi({ titulo, valor, tono }) {
  return (
    <div className={`kpi ${tono ?? ''}`}>
      <span>{titulo}</span>
      <strong>{valor}</strong>
    </div>
  )
}

export default function Dashboard() {
  const { datos, error, cargando, recargar } = useCarga(() => api('/dashboard'), [])
  useEventos(null, recargar) // cualquier cambio en vivo refresca los indicadores

  if (cargando) return <Cargando />
  if (error) return <MensajeError error={error} />
  const { kpis, porMes, porSeveridad, porEstado, urgentes } = datos
  const serie = porMes.map((p) => ({ ...p, mes: MESES[Number(p.mes.slice(5)) - 1] }))
  const dona = porSeveridad.filter((s) => s.total > 0)

  return (
    <div className="pila">
      <h1>Dashboard</h1>
      <div className="kpis">
        <Kpi titulo="Incidentes totales" valor={kpis.total} />
        <Kpi titulo="Abiertos" valor={kpis.abiertos} />
        <Kpi titulo="En revisión" valor={kpis.enRevision} />
        <Kpi titulo="Críticos abiertos" valor={kpis.criticosAbiertos} tono="alerta" />
        <Kpi titulo="Plazo vencido" valor={kpis.vencidos} tono={kpis.vencidos > 0 ? 'peligro' : ''} />
      </div>

      <div className="rejilla-2">
        <section className="tarjeta">
          <h2>Evolución (últimos 6 meses)</h2>
          <ResponsiveContainer width="100%" height={260}>
            <LineChart data={serie}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e3e8e8" />
              <XAxis dataKey="mes" />
              <YAxis allowDecimals={false} />
              <Tooltip />
              <Legend />
              <Line type="monotone" dataKey="registrados" name="Registrados" stroke="#0f3b3a" strokeWidth={2} />
              <Line type="monotone" dataKey="cerrados" name="Cerrados" stroke="#f28c28" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </section>
        <section className="tarjeta">
          <h2>Por severidad</h2>
          {dona.length === 0 ? <p className="nota">Aún no hay incidentes.</p> : (
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie data={dona} dataKey="total" nameKey="severidad" innerRadius={60} outerRadius={95} paddingAngle={2}>
                  {dona.map((s) => <Cell key={s.severidad} fill={COLOR_SEVERIDAD[s.severidad]} />)}
                </Pie>
                <Tooltip formatter={(v, n) => [v, etiqueta(n)]} />
                <Legend formatter={(v) => etiqueta(v)} />
              </PieChart>
            </ResponsiveContainer>
          )}
        </section>
      </div>

      <div className="rejilla-2">
        <section className="tarjeta">
          <h2>Por estado</h2>
          <ul className="barras">
            {porEstado.map((e) => (
              <li key={e.estado}>
                <span>{etiqueta(e.estado)}</span>
                <div className="barra-fondo"><div className={`barra-llena estado-${e.estado}`}
                  style={{ width: `${kpis.total ? (e.total * 100) / kpis.total : 0}%` }} /></div>
                <strong>{e.total}</strong>
              </li>
            ))}
          </ul>
        </section>
        <section className="tarjeta">
          <h2>Plazos más urgentes</h2>
          {urgentes.length === 0 ? <p className="nota">Nada pendiente.</p> : (
            <table className="tabla compacta">
              <tbody>
                {urgentes.map((i) => (
                  <tr key={i.id}>
                    <td><Link to={`/incidentes/${i.id}`}>{i.codigo}</Link><br /><small>{i.titulo}</small></td>
                    <td><Insignia tipo="severidad" valor={i.severidad} /></td>
                    <td className={i.vencido ? 'texto-peligro' : ''}>{textoPlazo(i.fechaLimiteReporte)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      </div>
    </div>
  )
}
