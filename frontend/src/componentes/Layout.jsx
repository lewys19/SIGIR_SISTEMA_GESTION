import { useCallback, useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { api } from '../api/cliente'
import { useAuth } from '../auth/AuthContext'
import { useEventos } from '../eventos/EventosContext'
import { etiqueta, fechaHora } from '../lib/formato'
import CambiarPassword from './CambiarPassword'
import { useCarga } from './ui'

const MENU = [
  { a: '/', texto: 'Dashboard', icono: '▦', permiso: 'dashboard:ver', exacto: true },
  { a: '/incidentes', texto: 'Incidentes', icono: '⚑', permiso: 'incidentes:ver', exacto: true },
  { a: '/incidentes/nuevo', texto: 'Registrar incidente', icono: '＋', permiso: 'incidentes:crear' },
  { a: '/revisiones', texto: 'Revisión y aprobaciones', icono: '✓', permiso: 'revision:aprobar' },
  { a: '/monitoreo', texto: 'Monitoreo de eventos', icono: '◉', permiso: 'monitoreo:ver' },
  { a: '/reportes', texto: 'Reportes', icono: '▤', permiso: 'reportes:ver' },
  { a: '/notificaciones', texto: 'Notificaciones', icono: '✉' },
  { a: '/usuarios', texto: 'Usuarios', icono: '☺', permiso: 'usuarios:gestionar' },
  { a: '/roles', texto: 'Roles y permisos', icono: '⚿', permiso: 'roles:gestionar' },
  { a: '/auditoria', texto: 'Auditoría', icono: '⌕', permiso: 'auditoria:ver' },
]

function Campana() {
  const navegar = useNavigate()
  const [abierta, setAbierta] = useState(false)
  const [avisos, setAvisos] = useState([])
  const { datos, recargar } = useCarga(() => api('/notificaciones'), [])

  const alLlegar = useCallback((n) => {
    recargar()
    const id = Date.now() + Math.random()
    setAvisos((a) => [...a, { id, ...n }])
    setTimeout(() => setAvisos((a) => a.filter((x) => x.id !== id)), 6000)
  }, [recargar])
  const conectado = useEventos('notificacion', alLlegar)

  const abrir = async (n) => {
    setAbierta(false)
    if (!n.leida) await api(`/notificaciones/${n.id}/leer`, { metodo: 'POST' })
    recargar()
    if (n.incidenteId) navegar(`/incidentes/${n.incidenteId}`)
  }

  return (
    <>
      <span className={`en-vivo ${conectado ? 'si' : 'no'}`} title={conectado ? 'Conectado en tiempo real' : 'Reconectando…'} />
      <div className="campana">
        <button className="btn-icono" onClick={() => setAbierta(!abierta)} aria-label="Notificaciones">
          🔔{datos?.noLeidas > 0 && <span className="contador">{datos.noLeidas}</span>}
        </button>
        {abierta && (
          <div className="desplegable">
            <div className="desplegable-cabecera">
              <strong>Notificaciones</strong>
              <Link to="/notificaciones" onClick={() => setAbierta(false)}>Ver todas</Link>
            </div>
            {datos?.items.length === 0 && <p className="nota">No tienes notificaciones.</p>}
            {datos?.items.slice(0, 6).map((n) => (
              <button key={n.id} className={`aviso ${n.leida ? '' : 'nueva'}`} onClick={() => abrir(n)}>
                <strong>{n.titulo}</strong>
                <small>{fechaHora(n.creadaEn)}</small>
              </button>
            ))}
          </div>
        )}
      </div>
      <div className="toasts">
        {avisos.map((a) => (
          <div key={a.id} className="toast" onClick={() => navegar(`/incidentes/${a.incidenteId}`)}>
            <strong>{a.mensaje}</strong>
            <small>{etiqueta(a.tipo)} · {a.actor}</small>
          </div>
        ))}
      </div>
    </>
  )
}

export default function Layout({ children }) {
  const { usuario, logout, puede } = useAuth()
  const [menuAbierto, setMenuAbierto] = useState(false)
  const [cambiarClave, setCambiarClave] = useState(false)

  return (
    <div className="layout">
      <aside className={`sidebar ${menuAbierto ? 'abierto' : ''}`}>
        <div className="marca">SIGIR</div>
        <nav onClick={() => setMenuAbierto(false)}>
          {MENU.filter((m) => !m.permiso || puede(m.permiso)).map((m) => (
            <NavLink key={m.a} to={m.a} end={m.exacto}>
              <span className="icono">{m.icono}</span>{m.texto}
            </NavLink>
          ))}
        </nav>
      </aside>
      {menuAbierto && <div className="velo" onClick={() => setMenuAbierto(false)} />}
      <div className="contenido">
        <header className="barra">
          <button className="btn-icono hamburguesa" onClick={() => setMenuAbierto(true)} aria-label="Menú">☰</button>
          <div className="barra-derecha">
            <Campana />
            <div className="quien">
              <strong>{usuario.nombre}</strong>
              <small>{etiqueta(usuario.rol)}</small>
            </div>
            <button className="btn-secundario" onClick={() => setCambiarClave(true)}>Contraseña</button>
            <button className="btn-secundario" onClick={logout}>Salir</button>
          </div>
        </header>
        <main>{children}</main>
      </div>
      {cambiarClave && <CambiarPassword onCerrar={() => setCambiarClave(false)} />}
    </div>
  )
}
