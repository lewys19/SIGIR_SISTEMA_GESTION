import { useNavigate } from 'react-router-dom'
import { api } from '../api/cliente'
import { useEventos } from '../eventos/EventosContext'
import { Cargando, MensajeError, useCarga } from '../componentes/ui'
import { etiqueta, fechaHora } from '../lib/formato'

export default function Notificaciones() {
  const navegar = useNavigate()
  const { datos, error, cargando, recargar } = useCarga(() => api('/notificaciones'), [])
  useEventos('notificacion', recargar)

  const abrir = async (n) => {
    if (!n.leida) await api(`/notificaciones/${n.id}/leer`, { metodo: 'POST' })
    if (n.incidenteId) navegar(`/incidentes/${n.incidenteId}`)
    else recargar()
  }
  const leerTodas = async () => { await api('/notificaciones/leer-todas', { metodo: 'POST' }); recargar() }

  return (
    <div className="pila">
      <div className="titulo-con-accion">
        <h1>Notificaciones</h1>
        <button className="btn-secundario" disabled={!datos?.noLeidas} onClick={leerTodas}>Marcar todas como leídas</button>
      </div>
      <MensajeError error={error} />
      {cargando ? <Cargando /> : (
        <section className="tarjeta sin-relleno">
          <ul className="feed">
            {datos.items.length === 0 && <li className="nota">No tienes notificaciones.</li>}
            {datos.items.map((n) => (
              <li key={n.id} className={`clicable ${n.leida ? '' : 'nuevo'}`} onClick={() => abrir(n)}>
                <span className={`punto evento-${n.tipo}`} />
                <div>
                  <strong>{n.titulo}</strong>
                  {n.mensaje && <p>{n.mensaje}</p>}
                  <small>{etiqueta(n.tipo)} · {fechaHora(n.creadaEn)}</small>
                </div>
                {!n.leida && <span className="insignia nueva">Nueva</span>}
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  )
}
