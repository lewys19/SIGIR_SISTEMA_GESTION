import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api/cliente'
import { useEventos } from '../eventos/EventosContext'
import { Campo, Cargando, Insignia, MensajeError, Modal, useCarga } from '../componentes/ui'
import { etiqueta, fecha, fechaHora, textoPlazo } from '../lib/formato'

function Decision({ tipo, onConfirmar, onCerrar }) {
  const [comentario, setComentario] = useState('')
  const [error, setError] = useState(null)
  const obligatorio = tipo === 'rechazar'
  const titulos = { aprobar: 'Aprobar incidente', rechazar: 'Rechazar incidente', cerrar: 'Cerrar incidente' }

  const confirmar = async () => {
    if (obligatorio && !comentario.trim()) return setError(new Error('Indica el motivo del rechazo'))
    try { await onConfirmar(comentario) } catch (e) { setError(e) }
  }

  return (
    <Modal titulo={titulos[tipo]} onCerrar={onCerrar}>
      <div className="formulario">
        <Campo etiqueta={obligatorio ? 'Motivo del rechazo *' : 'Comentario (opcional)'}>
          <textarea rows={4} maxLength={2000} value={comentario} onChange={(e) => setComentario(e.target.value)} autoFocus />
        </Campo>
        <MensajeError error={error} />
        <div className="acciones">
          <button className="btn-secundario" onClick={onCerrar}>Cancelar</button>
          <button className={tipo === 'rechazar' ? 'btn-peligro' : 'btn-principal'} onClick={confirmar}>Confirmar</button>
        </div>
      </div>
    </Modal>
  )
}

function Dato({ t, children }) {
  return <div className="dato"><span>{t}</span><div>{children || '—'}</div></div>
}

export default function IncidenteDetalle() {
  const { id } = useParams()
  const { datos: i, error, cargando, recargar } = useCarga(() => api(`/incidentes/${id}`), [id])
  const [decision, setDecision] = useState(null)
  const [errorAccion, setErrorAccion] = useState(null)
  useEventos(null, (e) => { if (String(e.incidenteId) === id) recargar() })

  if (cargando) return <Cargando />
  if (error) return <MensajeError error={error} />

  const enviar = async () => {
    try { await api(`/incidentes/${id}/enviar`, { metodo: 'POST' }); recargar() } catch (e) { setErrorAccion(e) }
  }
  const decidir = async (comentario) => {
    await api(`/revisiones/${id}/${decision}`, { metodo: 'POST', cuerpo: { comentario } })
    setDecision(null)
    recargar()
  }
  const finalizado = ['APROBADO', 'CERRADO'].includes(i.estado)

  return (
    <div className="pila">
      <div className="titulo-con-accion">
        <div>
          <small className="nota">{i.codigo}</small>
          <h1>{i.titulo}</h1>
          <div className="fila-insignias">
            <Insignia tipo="estado" valor={i.estado} />
            <Insignia tipo="severidad" valor={i.severidad} />
            {i.vencido && <span className="insignia vencido">Plazo vencido</span>}
          </div>
        </div>
        <div className="acciones">
          {i.acciones.includes('editar') && <Link className="btn-secundario" to={`/incidentes/${id}/editar`}>Editar</Link>}
          {i.acciones.includes('enviar') && <button className="btn-principal" onClick={enviar}>Enviar a revisión</button>}
          {i.acciones.includes('rechazar') && <button className="btn-peligro" onClick={() => setDecision('rechazar')}>Rechazar</button>}
          {i.acciones.includes('aprobar') && <button className="btn-principal" onClick={() => setDecision('aprobar')}>Aprobar</button>}
          {i.acciones.includes('cerrar') && <button className="btn-principal" onClick={() => setDecision('cerrar')}>Cerrar incidente</button>}
        </div>
      </div>
      <MensajeError error={errorAccion} />

      <div className="rejilla-2 ancho-izq">
        <section className="tarjeta">
          <h2>Detalle</h2>
          <p className="descripcion">{i.descripcion}</p>
          <div className="datos">
            <Dato t="Tipo">{etiqueta(i.tipo)}</Dato>
            <Dato t="Regulador">{i.regulador}</Dato>
            <Dato t="Área">{i.area}</Dato>
            <Dato t="Ocurrencia">{fecha(i.fechaOcurrencia)}</Dato>
            <Dato t="Detección">{fecha(i.fechaDeteccion)}</Dato>
            <Dato t="Límite para reportar">
              {fecha(i.fechaLimiteReporte)}
              {!finalizado && <small className={i.vencido ? 'texto-peligro' : 'nota'}> · {textoPlazo(i.fechaLimiteReporte)}</small>}
            </Dato>
            <Dato t="Reportado por">{i.reportadoPor.nombre}</Dato>
            <Dato t="Registrado">{fechaHora(i.creadoEn)}</Dato>
          </div>
          <h3>Impacto</h3>
          <p className="descripcion">{i.impacto || '—'}</p>
          <h3>Medidas adoptadas</h3>
          <p className="descripcion">{i.medidas || '—'}</p>
        </section>

        <section className="tarjeta">
          <h2>Historial</h2>
          {i.historial.length === 0 && <p className="nota">Aún no se envió a revisión.</p>}
          <ol className="linea-tiempo">
            {i.historial.map((h, n) => (
              <li key={n} className={`hito hito-${h.decision}`}>
                <strong>{etiqueta(h.decision)}</strong>
                <small>{h.usuario.nombre} · {fechaHora(h.fecha)}</small>
                {h.comentario && <p>“{h.comentario}”</p>}
              </li>
            ))}
          </ol>
        </section>
      </div>

      {decision && <Decision tipo={decision} onConfirmar={decidir} onCerrar={() => setDecision(null)} />}
    </div>
  )
}
