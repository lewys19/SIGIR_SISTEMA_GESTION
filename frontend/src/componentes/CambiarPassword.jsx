import { useState } from 'react'
import { api } from '../api/cliente'
import { Campo, MensajeError, Modal } from './ui'

export default function CambiarPassword({ onCerrar }) {
  const [f, setF] = useState({ actual: '', nueva: '', repetir: '' })
  const [error, setError] = useState(null)
  const [listo, setListo] = useState(false)

  const guardar = async (e) => {
    e.preventDefault()
    if (f.nueva !== f.repetir) return setError(new Error('Las contraseñas nuevas no coinciden'))
    try {
      await api('/auth/password', { metodo: 'PUT', cuerpo: { actual: f.actual, nueva: f.nueva } })
      setListo(true)
    } catch (err) {
      setError(err.estado === 401 ? new Error('La contraseña actual no es correcta') : err)
    }
  }

  return (
    <Modal titulo="Cambiar mi contraseña" onCerrar={onCerrar}>
      {listo ? (
        <><p>Contraseña actualizada.</p><button className="btn-principal" onClick={onCerrar}>Listo</button></>
      ) : (
        <form onSubmit={guardar} className="formulario">
          <Campo etiqueta="Contraseña actual">
            <input type="password" required value={f.actual} onChange={(e) => setF({ ...f, actual: e.target.value })} />
          </Campo>
          <Campo etiqueta="Nueva contraseña" ayuda="Mínimo 8 caracteres, con letras y números">
            <input type="password" required value={f.nueva} onChange={(e) => setF({ ...f, nueva: e.target.value })} />
          </Campo>
          <Campo etiqueta="Repite la nueva contraseña">
            <input type="password" required value={f.repetir} onChange={(e) => setF({ ...f, repetir: e.target.value })} />
          </Campo>
          <MensajeError error={error} />
          <button className="btn-principal">Guardar</button>
        </form>
      )}
    </Modal>
  )
}
