import { useCallback, useEffect, useState } from 'react'
import { etiqueta } from '../lib/formato'

export function Insignia({ valor, tipo }) {
  return <span className={`insignia ${tipo}-${valor}`}>{etiqueta(valor)}</span>
}

export function Cargando() {
  return <div className="cargando">Cargando…</div>
}

export function MensajeError({ error }) {
  if (!error) return null
  return <div className="error" role="alert">{error.message ?? String(error)}</div>
}

export function Campo({ etiqueta: texto, error, children, ayuda }) {
  return (
    <label className="campo">
      <span>{texto}</span>
      {children}
      {ayuda && <small className="ayuda">{ayuda}</small>}
      {error && <small className="error-campo">{error}</small>}
    </label>
  )
}

export function Modal({ titulo, onCerrar, children }) {
  useEffect(() => {
    const esc = (e) => e.key === 'Escape' && onCerrar()
    window.addEventListener('keydown', esc)
    return () => window.removeEventListener('keydown', esc)
  }, [onCerrar])
  return (
    <div className="modal-fondo" onMouseDown={onCerrar}>
      <div className="modal" role="dialog" aria-label={titulo} onMouseDown={(e) => e.stopPropagation()}>
        <div className="modal-cabecera">
          <h3>{titulo}</h3>
          <button className="btn-icono" onClick={onCerrar} aria-label="Cerrar">✕</button>
        </div>
        {children}
      </div>
    </div>
  )
}

export function Paginacion({ pagina, total, tamano, onCambiar }) {
  const paginas = Math.max(1, Math.ceil(total / tamano))
  return (
    <div className="paginacion">
      <span>{total} resultado{total === 1 ? '' : 's'}</span>
      <button className="btn-secundario" disabled={pagina === 0} onClick={() => onCambiar(pagina - 1)}>‹ Anterior</button>
      <span>Página {pagina + 1} de {paginas}</span>
      <button className="btn-secundario" disabled={pagina + 1 >= paginas} onClick={() => onCambiar(pagina + 1)}>Siguiente ›</button>
    </div>
  )
}

/** Carga datos asíncronos con estado de carga/error y una función para recargar. */
export function useCarga(cargar, deps) {
  const [datos, setDatos] = useState(null)
  const [error, setError] = useState(null)
  const [cargando, setCargando] = useState(true)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  const ejecutar = useCallback(cargar, deps)

  const recargar = useCallback(async () => {
    try {
      setDatos(await ejecutar())
      setError(null)
    } catch (e) {
      setError(e)
    } finally {
      setCargando(false)
    }
  }, [ejecutar])

  useEffect(() => { recargar() }, [recargar])
  return { datos, error, cargando, recargar }
}
