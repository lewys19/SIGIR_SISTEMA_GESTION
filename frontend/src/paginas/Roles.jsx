import { useEffect, useState } from 'react'
import { api } from '../api/cliente'
import { useAuth } from '../auth/AuthContext'
import { Cargando, MensajeError } from '../componentes/ui'
import { etiqueta, etiquetaPermiso } from '../lib/formato'

export default function Roles() {
  const { recargar: recargarMisPermisos } = useAuth()
  const [catalogo, setCatalogo] = useState(null)
  const [matriz, setMatriz] = useState({})   // { ROL: Set(permisos) }
  const [original, setOriginal] = useState({})
  const [error, setError] = useState(null)
  const [mensaje, setMensaje] = useState('')

  const cargar = async () => {
    const d = await api('/roles')
    const m = Object.fromEntries(d.roles.map((r) => [r.rol, new Set(r.permisos)]))
    setCatalogo(d.catalogo)
    setMatriz(m)
    setOriginal(Object.fromEntries(Object.entries(m).map(([k, v]) => [k, new Set(v)])))
  }
  useEffect(() => { cargar().catch(setError) }, [])

  const alternar = (rol, permiso) => {
    const s = new Set(matriz[rol])
    if (s.has(permiso)) s.delete(permiso)
    else s.add(permiso)
    setMatriz({ ...matriz, [rol]: s })
  }
  const cambiado = (rol) => {
    const a = matriz[rol]
    const b = original[rol]
    return a.size !== b.size || [...a].some((p) => !b.has(p))
  }
  const guardar = async () => {
    setError(null)
    try {
      for (const rol of Object.keys(matriz).filter(cambiado)) {
        await api(`/roles/${rol}`, { metodo: 'PUT', cuerpo: { permisos: [...matriz[rol]] } })
      }
      await cargar()
      await recargarMisPermisos()
      setMensaje('Permisos guardados. Se aplican de inmediato, sin que nadie tenga que volver a entrar.')
    } catch (e) { setError(e) }
  }

  if (!catalogo) return error ? <MensajeError error={error} /> : <Cargando />
  const roles = Object.keys(matriz)
  const hayCambios = roles.some(cambiado)

  return (
    <div className="pila">
      <div className="titulo-con-accion">
        <h1>Roles y permisos</h1>
        <button className="btn-principal" disabled={!hayCambios} onClick={guardar}>Guardar cambios</button>
      </div>
      <MensajeError error={error} />
      {mensaje && !hayCambios && <div className="exito">{mensaje}</div>}
      <section className="tarjeta sin-relleno">
        <div className="tabla-scroll">
          <table className="tabla matriz">
            <thead><tr><th>Permiso</th>{roles.map((r) => <th key={r}>{etiqueta(r)}{cambiado(r) && ' •'}</th>)}</tr></thead>
            <tbody>
              {catalogo.map((p) => (
                <tr key={p}>
                  <td>{etiquetaPermiso(p)}<br /><small className="nota">{p}</small></td>
                  {roles.map((r) => {
                    const bloqueado = r === 'ADMIN' && p === 'roles:gestionar'
                    return (
                      <td key={r} className="centro">
                        <input type="checkbox" aria-label={`${r} ${p}`} checked={matriz[r].has(p)} disabled={bloqueado}
                          title={bloqueado ? 'El administrador siempre conserva este permiso' : ''}
                          onChange={() => alternar(r, p)} />
                      </td>
                    )
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  )
}
