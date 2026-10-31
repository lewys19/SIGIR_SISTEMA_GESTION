import { Navigate } from 'react-router-dom'
import { useAuth } from './AuthContext'

/** Exige sesión y, opcionalmente, un permiso. */
export default function RutaProtegida({ permiso, children }) {
  const { usuario, cargando, puede } = useAuth()
  if (cargando) return <div className="cargando">Cargando…</div>
  if (!usuario) return <Navigate to="/login" replace />
  if (permiso && !puede(permiso)) {
    return <div className="tarjeta"><h2>Sin acceso</h2><p>Tu rol no tiene permiso para ver esta sección.</p></div>
  }
  return children
}
