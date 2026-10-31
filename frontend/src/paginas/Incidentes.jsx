import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api, query } from '../api/cliente'
import { useAuth } from '../auth/AuthContext'
import { useEventos } from '../eventos/EventosContext'
import { Cargando, Insignia, MensajeError, Paginacion, useCarga } from '../componentes/ui'
import { etiqueta, fecha, textoPlazo } from '../lib/formato'

export default function Incidentes() {
  const { puede } = useAuth()
  const [filtro, setFiltro] = useState({ q: '', estado: '', severidad: '', tipo: '' })
  const [pagina, setPagina] = useState(0)
  const catalogos = useCarga(() => api('/incidentes/catalogos'), [])
  const lista = useCarga(() => api(`/incidentes${query({ ...filtro, pagina, tamano: 15 })}`), [filtro, pagina])
  useEventos(null, lista.recargar)

  const cambiar = (campo) => (e) => { setPagina(0); setFiltro({ ...filtro, [campo]: e.target.value }) }
  const c = catalogos.datos

  return (
    <div className="pila">
      <div className="titulo-con-accion">
        <h1>Incidentes</h1>
        {puede('incidentes:crear') && <Link className="btn-principal" to="/incidentes/nuevo">＋ Registrar incidente</Link>}
      </div>

      <div className="filtros tarjeta">
        <input placeholder="Buscar por código o título…" value={filtro.q} onChange={cambiar('q')} />
        {c && <>
          <select value={filtro.estado} onChange={cambiar('estado')}>
            <option value="">Todos los estados</option>
            {c.estados.map((v) => <option key={v} value={v}>{etiqueta(v)}</option>)}
          </select>
          <select value={filtro.severidad} onChange={cambiar('severidad')}>
            <option value="">Toda severidad</option>
            {c.severidades.map((v) => <option key={v} value={v}>{etiqueta(v)}</option>)}
          </select>
          <select value={filtro.tipo} onChange={cambiar('tipo')}>
            <option value="">Todos los tipos</option>
            {c.tipos.map((v) => <option key={v} value={v}>{etiqueta(v)}</option>)}
          </select>
        </>}
      </div>

      <MensajeError error={lista.error} />
      {lista.cargando ? <Cargando /> : lista.datos && (
        <section className="tarjeta sin-relleno">
          <div className="tabla-scroll">
            <table className="tabla">
              <thead>
                <tr><th>Código</th><th>Título</th><th>Tipo</th><th>Severidad</th><th>Estado</th><th>Regulador</th><th>Plazo</th><th>Reportado por</th></tr>
              </thead>
              <tbody>
                {lista.datos.items.length === 0 && <tr><td colSpan="8" className="nota">No hay incidentes con estos filtros.</td></tr>}
                {lista.datos.items.map((i) => (
                  <tr key={i.id}>
                    <td><Link to={`/incidentes/${i.id}`}>{i.codigo}</Link></td>
                    <td>{i.titulo}</td>
                    <td>{etiqueta(i.tipo)}</td>
                    <td><Insignia tipo="severidad" valor={i.severidad} /></td>
                    <td><Insignia tipo="estado" valor={i.estado} /></td>
                    <td>{i.regulador}</td>
                    <td className={i.vencido ? 'texto-peligro' : ''} title={fecha(i.fechaLimiteReporte)}>
                      {['APROBADO', 'CERRADO'].includes(i.estado) ? fecha(i.fechaLimiteReporte) : textoPlazo(i.fechaLimiteReporte)}
                    </td>
                    <td>{i.reportadoPor.nombre}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginacion pagina={pagina} total={lista.datos.total} tamano={15} onCambiar={setPagina} />
        </section>
      )}
    </div>
  )
}
