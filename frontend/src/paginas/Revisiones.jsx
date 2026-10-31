import { Link } from 'react-router-dom'
import { api } from '../api/cliente'
import { useAuth } from '../auth/AuthContext'
import { useEventos } from '../eventos/EventosContext'
import { Cargando, Insignia, MensajeError, useCarga } from '../componentes/ui'
import { etiqueta, textoPlazo } from '../lib/formato'

export default function Revisiones() {
  const { usuario } = useAuth()
  const { datos, error, cargando, recargar } = useCarga(() => api('/revisiones/pendientes'), [])
  useEventos(null, recargar)

  return (
    <div className="pila">
      <h1>Revisión y aprobaciones</h1>
      <p className="nota">Incidentes enviados a revisión, ordenados por plazo. No puedes revisar los que registraste tú.</p>
      <MensajeError error={error} />
      {cargando ? <Cargando /> : (
        <section className="tarjeta sin-relleno">
          <div className="tabla-scroll">
            <table className="tabla">
              <thead><tr><th>Código</th><th>Título</th><th>Tipo</th><th>Severidad</th><th>Plazo</th><th>Reportado por</th><th /></tr></thead>
              <tbody>
                {datos.length === 0 && <tr><td colSpan="7" className="nota">No hay incidentes pendientes de revisión. 🎉</td></tr>}
                {datos.map((i) => {
                  const propio = String(i.reportadoPor.id) === String(usuario.id)
                  return (
                    <tr key={i.id}>
                      <td>{i.codigo}</td>
                      <td>{i.titulo}</td>
                      <td>{etiqueta(i.tipo)}</td>
                      <td><Insignia tipo="severidad" valor={i.severidad} /></td>
                      <td className={i.vencido ? 'texto-peligro' : ''}>{textoPlazo(i.fechaLimiteReporte)}</td>
                      <td>{i.reportadoPor.nombre}{propio && <small className="nota"> (tú)</small>}</td>
                      <td><Link className="btn-secundario" to={`/incidentes/${i.id}`}>{propio ? 'Ver' : 'Revisar'}</Link></td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </section>
      )}
    </div>
  )
}
