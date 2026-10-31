import { createContext, useContext, useEffect, useRef, useState } from 'react'
import { api } from '../api/cliente'
import { useAuth } from '../auth/AuthContext'

const EventosContext = createContext(null)

/**
 * Una sola conexión WebSocket por pestaña. Pide un ticket de un uso (con el JWT),
 * se conecta a /ws/eventos y reconecta sola si se corta.
 */
export function EventosProvider({ children }) {
  const { usuario } = useAuth()
  const oyentes = useRef(new Set())
  const [conectado, setConectado] = useState(false)

  useEffect(() => {
    if (!usuario) return undefined
    let ws
    let cerradoAProposito = false
    let espera = 1000
    let temporizador

    const conectar = async () => {
      try {
        const { ticket } = await api('/notificaciones/ticket', { metodo: 'POST' })
        const protocolo = location.protocol === 'https:' ? 'wss' : 'ws'
        ws = new WebSocket(`${protocolo}://${location.host}/ws/eventos?ticket=${encodeURIComponent(ticket)}`)
        ws.onopen = () => { setConectado(true); espera = 1000 }
        ws.onmessage = (m) => {
          const dato = JSON.parse(m.data)
          oyentes.current.forEach((fn) => fn(dato))
        }
        ws.onclose = () => {
          setConectado(false)
          if (!cerradoAProposito) reintentar()
        }
      } catch {
        reintentar()
      }
    }
    const reintentar = () => {
      temporizador = setTimeout(conectar, espera)
      espera = Math.min(espera * 2, 30000)
    }

    conectar()
    return () => {
      cerradoAProposito = true
      clearTimeout(temporizador)
      ws?.close()
    }
  }, [usuario])

  const suscribir = (fn) => {
    oyentes.current.add(fn)
    return () => oyentes.current.delete(fn)
  }

  return <EventosContext.Provider value={{ conectado, suscribir }}>{children}</EventosContext.Provider>
}

/** Ejecuta fn con cada mensaje del canal indicado ('evento' o 'notificacion'). */
export function useEventos(canal, fn) {
  const ctx = useContext(EventosContext)
  const ref = useRef(fn)
  useEffect(() => { ref.current = fn })
  useEffect(() => ctx.suscribir((d) => { if (!canal || d.canal === canal) ref.current(d) }), [ctx, canal])
  return ctx.conectado
}
