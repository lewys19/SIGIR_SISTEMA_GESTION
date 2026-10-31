import { BrowserRouter, Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth/AuthContext'
import RutaProtegida from './auth/RutaProtegida'
import Layout from './componentes/Layout'
import { EventosProvider } from './eventos/EventosContext'
import Auditoria from './paginas/Auditoria'
import Dashboard from './paginas/Dashboard'
import IncidenteDetalle from './paginas/IncidenteDetalle'
import Incidentes from './paginas/Incidentes'
import Login from './paginas/Login'
import Monitoreo from './paginas/Monitoreo'
import Notificaciones from './paginas/Notificaciones'
import RegistroIncidente from './paginas/RegistroIncidente'
import Reportes from './paginas/Reportes'
import Revisiones from './paginas/Revisiones'
import Roles from './paginas/Roles'
import Usuarios from './paginas/Usuarios'

const RUTAS = [
  ['/incidentes', 'incidentes:ver', Incidentes],
  ['/incidentes/nuevo', 'incidentes:crear', RegistroIncidente],
  ['/incidentes/:id', 'incidentes:ver', IncidenteDetalle],
  ['/incidentes/:id/editar', 'incidentes:editar', RegistroIncidente],
  ['/revisiones', 'revision:aprobar', Revisiones],
  ['/monitoreo', 'monitoreo:ver', Monitoreo],
  ['/reportes', 'reportes:ver', Reportes],
  ['/usuarios', 'usuarios:gestionar', Usuarios],
  ['/roles', 'roles:gestionar', Roles],
  ['/auditoria', 'auditoria:ver', Auditoria],
  ['/notificaciones', null, Notificaciones],
]

/** La portada es el dashboard; si el rol no lo tiene, la lista de incidentes. */
function Inicio() {
  const { puede } = useAuth()
  if (puede('dashboard:ver')) return <Dashboard />
  if (puede('incidentes:ver')) return <Navigate to="/incidentes" replace />
  return <Navigate to="/notificaciones" replace />
}

/** Sesión + WebSocket + menú: se montan UNA vez y las páginas cambian dentro (Outlet). */
function Aplicacion() {
  return (
    <RutaProtegida>
      <EventosProvider>
        <Layout><Outlet /></Layout>
      </EventosProvider>
    </RutaProtegida>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<Aplicacion />}>
            <Route path="/" element={<Inicio />} />
            {RUTAS.map(([ruta, permiso, Pagina]) => (
              <Route key={ruta} path={ruta} element={<RutaProtegida permiso={permiso}><Pagina /></RutaProtegida>} />
            ))}
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
