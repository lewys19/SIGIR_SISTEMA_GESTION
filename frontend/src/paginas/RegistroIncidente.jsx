import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { api } from '../api/cliente'
import { Campo, Cargando, MensajeError, useCarga } from '../componentes/ui'
import { etiqueta, fecha, hoyIso } from '../lib/formato'

const DIAS_PLAZO = { BAJA: 15, MEDIA: 7, ALTA: 3, CRITICA: 1 }
const VACIO = {
  titulo: '', descripcion: '', tipo: '', severidad: '', regulador: '', area: '',
  fechaOcurrencia: '', fechaDeteccion: hoyIso(), impacto: '', medidas: '',
}

function sumarDias(iso, dias) {
  const [a, m, d] = iso.split('-').map(Number)
  const r = new Date(Date.UTC(a, m - 1, d + dias))
  return r.toISOString().slice(0, 10)
}

export function validar(f, hoy = hoyIso()) {
  const e = {}
  if (!f.titulo.trim()) e.titulo = 'Obligatorio'
  if (!f.descripcion.trim()) e.descripcion = 'Obligatorio'
  if (!f.tipo) e.tipo = 'Elige un tipo'
  if (!f.severidad) e.severidad = 'Elige la severidad'
  if (!f.regulador) e.regulador = 'Elige el regulador'
  if (!f.fechaOcurrencia) e.fechaOcurrencia = 'Obligatorio'
  else if (f.fechaOcurrencia > hoy) e.fechaOcurrencia = 'No puede ser futura'
  if (!f.fechaDeteccion) e.fechaDeteccion = 'Obligatorio'
  else if (f.fechaDeteccion > hoy) e.fechaDeteccion = 'No puede ser futura'
  else if (f.fechaOcurrencia && f.fechaDeteccion < f.fechaOcurrencia) e.fechaDeteccion = 'No puede ser anterior a la ocurrencia'
  return e
}

export default function RegistroIncidente() {
  const { id } = useParams()
  const navegar = useNavigate()
  const catalogos = useCarga(() => api('/incidentes/catalogos'), [])
  const [f, setF] = useState(VACIO)
  const [errores, setErrores] = useState({})
  const [error, setError] = useState(null)
  const [guardando, setGuardando] = useState(false)
  const [cargandoDatos, setCargandoDatos] = useState(Boolean(id))

  useEffect(() => {
    if (!id) return
    api(`/incidentes/${id}`).then((i) => {
      setF(Object.fromEntries(Object.keys(VACIO).map((k) => [k, i[k] ?? ''])))
      setCargandoDatos(false)
    }).catch(setError)
  }, [id])

  const cambiar = (campo) => (e) => setF({ ...f, [campo]: e.target.value })

  const guardar = async (enviar) => {
    const e = validar(f)
    setErrores(e)
    if (Object.keys(e).length) return
    setGuardando(true)
    setError(null)
    try {
      const guardado = id
        ? await api(`/incidentes/${id}`, { metodo: 'PUT', cuerpo: f })
        : await api('/incidentes', { metodo: 'POST', cuerpo: f })
      if (enviar) await api(`/incidentes/${guardado.id}/enviar`, { metodo: 'POST' })
      navegar(`/incidentes/${guardado.id}`)
    } catch (err) {
      setError(err)
      setGuardando(false)
    }
  }

  if (catalogos.cargando || cargandoDatos) return error ? <MensajeError error={error} /> : <Cargando />
  const c = catalogos.datos
  const plazo = f.severidad && f.fechaDeteccion ? sumarDias(f.fechaDeteccion, DIAS_PLAZO[f.severidad]) : null

  return (
    <div className="pila">
      <h1>{id ? 'Editar incidente' : 'Registrar incidente'}</h1>
      <form className="tarjeta formulario" onSubmit={(e) => { e.preventDefault(); guardar(false) }}>
        <h2>Datos generales</h2>
        <Campo etiqueta="Título *" error={errores.titulo}>
          <input maxLength={200} value={f.titulo} onChange={cambiar('titulo')} />
        </Campo>
        <Campo etiqueta="Descripción *" error={errores.descripcion}>
          <textarea rows={4} maxLength={5000} value={f.descripcion} onChange={cambiar('descripcion')} />
        </Campo>
        <div className="rejilla-3">
          <Campo etiqueta="Tipo *" error={errores.tipo}>
            <select value={f.tipo} onChange={cambiar('tipo')}>
              <option value="">Elegir…</option>
              {c.tipos.map((v) => <option key={v} value={v}>{etiqueta(v)}</option>)}
            </select>
          </Campo>
          <Campo etiqueta="Severidad *" error={errores.severidad}>
            <select value={f.severidad} onChange={cambiar('severidad')}>
              <option value="">Elegir…</option>
              {c.severidades.map((v) => <option key={v} value={v}>{etiqueta(v)} — {DIAS_PLAZO[v]} día{DIAS_PLAZO[v] > 1 ? 's' : ''} de plazo</option>)}
            </select>
          </Campo>
          <Campo etiqueta="Regulador *" error={errores.regulador}>
            <select value={f.regulador} onChange={cambiar('regulador')}>
              <option value="">Elegir…</option>
              {c.reguladores.map((v) => <option key={v} value={v}>{v}</option>)}
            </select>
          </Campo>
        </div>
        <div className="rejilla-3">
          <Campo etiqueta="Fecha de ocurrencia *" error={errores.fechaOcurrencia}>
            <input type="date" max={hoyIso()} value={f.fechaOcurrencia} onChange={cambiar('fechaOcurrencia')} />
          </Campo>
          <Campo etiqueta="Fecha de detección *" error={errores.fechaDeteccion}>
            <input type="date" max={hoyIso()} value={f.fechaDeteccion} onChange={cambiar('fechaDeteccion')} />
          </Campo>
          <Campo etiqueta="Área afectada">
            <input maxLength={80} value={f.area} onChange={cambiar('area')} />
          </Campo>
        </div>
        {plazo && <div className="aviso-plazo">Plazo para reportar al regulador: <strong>{fecha(plazo)}</strong></div>}

        <h2>Evaluación</h2>
        <Campo etiqueta="Impacto">
          <textarea rows={3} maxLength={5000} value={f.impacto} onChange={cambiar('impacto')}
            placeholder="Clientes afectados, montos, sistemas comprometidos…" />
        </Campo>
        <Campo etiqueta="Medidas adoptadas">
          <textarea rows={3} maxLength={5000} value={f.medidas} onChange={cambiar('medidas')} />
        </Campo>

        <MensajeError error={error} />
        <div className="acciones">
          <button type="button" className="btn-secundario" onClick={() => navegar(-1)}>Cancelar</button>
          <button className="btn-secundario" disabled={guardando}>Guardar borrador</button>
          <button type="button" className="btn-principal" disabled={guardando} onClick={() => guardar(true)}>
            Guardar y enviar a revisión
          </button>
        </div>
      </form>
    </div>
  )
}
