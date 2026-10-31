import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/cliente'
import { useEventos } from '../eventos/EventosContext'
import { Cargando, Insignia, MensajeError } from '../componentes/ui'
import { etiqueta, fechaHora } from '../lib/formato'

export default function Monitoreo() {
  const [eventos, setEventos] = useState(null)
  const [estado, setEstado] = useState(null)
  const [error, setError] = useState(null)
  const [pausado, setPausado] = useState(false)

  useEffect(() => {
    Promise.all([api('/monitoreo/eventos?limite=100'), api('/monitoreo/estado')])
      .then(([e, s]) => { setEventos(e); setEstado(s) })
      .catch(setError)
  }, [])

  const conectado = useEventos('evento', (e) => {
    if (pausado) return
    setEventos((lista) => [{ ...e, id: `${e.fecha}-${e.tipo}-${e.incidenteId}`, nuevo: true }, ...(lista ?? [])].slice(0, 200))
    setEstado((s) => s && { ...s, eventosEnStream: s.eventosEnStream + 1 })
  })

  if (error) return <MensajeError error={error} />
  if (!eventos) return <Cargando />

  return (
    <div className="pila">
      <div className="titulo-con-accion">
        <h1>Monitoreo de eventos</h1>
        <button className="btn-secundario" onClick={() => setPausado(!pausado)}>{pausado ? '▶ Reanudar' : '⏸ Pausar'}</button>
      </div>
      <div className="kpis">
        <div className="kpi"><span>Canal en vivo</span><strong className={conectado ? 'texto-ok' : 'texto-peligro'}>{conectado ? 'Conectado' : 'Reconectando…'}</strong></div>
        <div className="kpi"><span>Eventos en Redis Stream</span><strong>{estado?.eventosEnStream ?? '—'}</strong></div>
        <div className="kpi"><span>Navegadores conectados</span><strong>{estado?.conexionesActivas ?? '—'}</strong></div>
      </div>
      <section className="tarjeta sin-relleno">
        <ul className="feed">
          {eventos.length === 0 && <li className="nota">Todavía no hay eventos.</li>}
          {eventos.map((e) => (
            <li key={e.id} className={e.nuevo ? 'nuevo' : ''}>
              <span className={`punto evento-${e.tipo}`} />
              <div>
                <span><strong>{etiqueta(e.tipo)}</strong> · <Link to={`/incidentes/${e.incidenteId}`}>{e.codigo}</Link> — {e.titulo}</span>
                <small>{e.actor} · {fechaHora(e.fecha)}</small>
              </div>
              <Insignia tipo="severidad" valor={e.severidad} />
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
