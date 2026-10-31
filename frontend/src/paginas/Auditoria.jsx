import { useState } from 'react'
import { api, query } from '../api/cliente'
import { Campo, Cargando, MensajeError, Paginacion, useCarga } from '../componentes/ui'
import { fechaHora } from '../lib/formato'

export default function Auditoria() {
  const [filtro, setFiltro] = useState({ accion: '', actor: '', desde: '', hasta: '' })
  const [pagina, setPagina] = useState(0)
  const acciones = useCarga(() => api('/auditoria/acciones'), [])
  const lista = useCarga(() => api(`/auditoria${query({ ...filtro, pagina, tamano: 25 })}`), [filtro, pagina])
  const cambiar = (campo) => (e) => { setPagina(0); setFiltro({ ...filtro, [campo]: e.target.value }) }

  return (
    <div className="pila">
      <h1>Auditoría</h1>
      <p className="nota">Bitácora inmutable: la base de datos rechaza cualquier intento de modificar o borrar registros.</p>
      <div className="filtros tarjeta">
        <Campo etiqueta="Acción">
          <select value={filtro.accion} onChange={cambiar('accion')}>
            <option value="">Todas</option>
            {(acciones.datos ?? []).map((a) => <option key={a} value={a}>{a}</option>)}
          </select>
        </Campo>
        <Campo etiqueta="Usuario"><input placeholder="correo…" value={filtro.actor} onChange={cambiar('actor')} /></Campo>
        <Campo etiqueta="Desde"><input type="date" value={filtro.desde} onChange={cambiar('desde')} /></Campo>
        <Campo etiqueta="Hasta"><input type="date" value={filtro.hasta} onChange={cambiar('hasta')} /></Campo>
      </div>
      <MensajeError error={lista.error} />
      {lista.cargando ? <Cargando /> : lista.datos && (
        <section className="tarjeta sin-relleno">
          <div className="tabla-scroll">
            <table className="tabla">
              <thead><tr><th>Fecha</th><th>Usuario</th><th>Acción</th><th>Entidad</th><th>Detalle</th></tr></thead>
              <tbody>
                {lista.datos.items.length === 0 && <tr><td colSpan="5" className="nota">Sin registros.</td></tr>}
                {lista.datos.items.map((r) => (
                  <tr key={r.id}>
                    <td className="nowrap">{fechaHora(r.fecha)}</td>
                    <td>{r.actor}</td>
                    <td><code className={r.accion === 'LOGIN_FALLIDO' ? 'texto-peligro' : ''}>{r.accion}</code></td>
                    <td>{r.entidad}{r.entidadId && ` #${r.entidadId}`}</td>
                    <td className="detalle">{r.detalle}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginacion pagina={pagina} total={lista.datos.total} tamano={25} onCambiar={setPagina} />
        </section>
      )}
    </div>
  )
}
