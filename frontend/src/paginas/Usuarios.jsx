import { useState } from 'react'
import { api } from '../api/cliente'
import { useAuth } from '../auth/AuthContext'
import { Campo, Cargando, MensajeError, Modal, useCarga } from '../componentes/ui'
import { etiqueta, fechaHora } from '../lib/formato'

const ROLES = ['ADMIN', 'SUPERVISOR', 'ANALISTA', 'AUDITOR']

function FormUsuario({ usuario, onGuardado, onCerrar }) {
  const nuevo = !usuario
  const [f, setF] = useState(usuario
    ? { nombre: usuario.nombre, rol: usuario.rol, activo: usuario.activo }
    : { email: '', nombre: '', rol: 'ANALISTA', password: '' })
  const [error, setError] = useState(null)

  const guardar = async (e) => {
    e.preventDefault()
    try {
      if (nuevo) await api('/usuarios', { metodo: 'POST', cuerpo: f })
      else await api(`/usuarios/${usuario.id}`, { metodo: 'PUT', cuerpo: f })
      onGuardado()
    } catch (err) { setError(err) }
  }

  return (
    <Modal titulo={nuevo ? 'Nuevo usuario' : `Editar ${usuario.email}`} onCerrar={onCerrar}>
      <form className="formulario" onSubmit={guardar}>
        {nuevo && <Campo etiqueta="Correo *"><input type="email" required value={f.email} onChange={(e) => setF({ ...f, email: e.target.value })} /></Campo>}
        <Campo etiqueta="Nombre *"><input required maxLength={120} value={f.nombre} onChange={(e) => setF({ ...f, nombre: e.target.value })} /></Campo>
        <Campo etiqueta="Rol">
          <select value={f.rol} onChange={(e) => setF({ ...f, rol: e.target.value })}>
            {ROLES.map((r) => <option key={r} value={r}>{etiqueta(r)}</option>)}
          </select>
        </Campo>
        {nuevo
          ? <Campo etiqueta="Contraseña inicial *" ayuda="Mínimo 8 caracteres, con letras y números">
              <input type="password" required value={f.password} onChange={(e) => setF({ ...f, password: e.target.value })} />
            </Campo>
          : <label className="check"><input type="checkbox" checked={f.activo} onChange={(e) => setF({ ...f, activo: e.target.checked })} /> Usuario activo</label>}
        <MensajeError error={error} />
        <button className="btn-principal">Guardar</button>
      </form>
    </Modal>
  )
}

function ResetPassword({ usuario, onCerrar }) {
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [listo, setListo] = useState(false)
  const guardar = async (e) => {
    e.preventDefault()
    try { await api(`/usuarios/${usuario.id}/password`, { metodo: 'PUT', cuerpo: { password } }); setListo(true) } catch (err) { setError(err) }
  }
  return (
    <Modal titulo={`Nueva contraseña para ${usuario.email}`} onCerrar={onCerrar}>
      {listo ? <><p>Contraseña cambiada. Comunícasela al usuario por un canal seguro.</p><button className="btn-principal" onClick={onCerrar}>Listo</button></> : (
        <form className="formulario" onSubmit={guardar}>
          <Campo etiqueta="Nueva contraseña" ayuda="Mínimo 8 caracteres, con letras y números">
            <input type="password" required value={password} onChange={(e) => setPassword(e.target.value)} />
          </Campo>
          <MensajeError error={error} />
          <button className="btn-principal">Cambiar</button>
        </form>
      )}
    </Modal>
  )
}

export default function Usuarios() {
  const { usuario: yo } = useAuth()
  const { datos, error, cargando, recargar } = useCarga(() => api('/usuarios'), [])
  const [editando, setEditando] = useState(undefined) // undefined = cerrado, null = nuevo
  const [reseteando, setReseteando] = useState(null)

  return (
    <div className="pila">
      <div className="titulo-con-accion">
        <h1>Usuarios</h1>
        <button className="btn-principal" onClick={() => setEditando(null)}>＋ Nuevo usuario</button>
      </div>
      <MensajeError error={error} />
      {cargando ? <Cargando /> : (
        <section className="tarjeta sin-relleno">
          <div className="tabla-scroll">
            <table className="tabla">
              <thead><tr><th>Nombre</th><th>Correo</th><th>Rol</th><th>Estado</th><th>Alta</th><th /></tr></thead>
              <tbody>
                {datos.map((u) => (
                  <tr key={u.id} className={u.activo ? '' : 'inactivo'}>
                    <td>{u.nombre}{String(u.id) === String(yo.id) && <small className="nota"> (tú)</small>}</td>
                    <td>{u.email}</td>
                    <td>{etiqueta(u.rol)}</td>
                    <td>{u.activo ? 'Activo' : 'Inactivo'}</td>
                    <td>{fechaHora(u.creadoEn)}</td>
                    <td className="acciones">
                      <button className="btn-secundario" onClick={() => setEditando(u)}>Editar</button>
                      <button className="btn-secundario" onClick={() => setReseteando(u)}>Contraseña</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}
      {editando !== undefined && (
        <FormUsuario usuario={editando} onCerrar={() => setEditando(undefined)} onGuardado={() => { setEditando(undefined); recargar() }} />
      )}
      {reseteando && <ResetPassword usuario={reseteando} onCerrar={() => setReseteando(null)} />}
    </div>
  )
}
