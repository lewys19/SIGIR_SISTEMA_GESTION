import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { api, borrarToken, guardarToken, tokenGuardado } from '../api/cliente'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null)
  const [cargando, setCargando] = useState(Boolean(tokenGuardado()))

  const cargarUsuario = useCallback(async () => {
    try {
      setUsuario(await api('/auth/me'))
    } catch {
      borrarToken() // token vencido o inválido
      setUsuario(null)
    } finally {
      setCargando(false)
    }
  }, [])

  const logout = useCallback(() => {
    borrarToken()
    setUsuario(null)
  }, [])

  useEffect(() => {
    if (tokenGuardado()) cargarUsuario()
    window.addEventListener('sigir:sesion-expirada', logout)
    return () => window.removeEventListener('sigir:sesion-expirada', logout)
  }, [cargarUsuario, logout])

  const login = async (email, password) => {
    const { token } = await api('/auth/login', { metodo: 'POST', cuerpo: { email, password } })
    guardarToken(token)
    await cargarUsuario()
  }

  const puede = useCallback((permiso) => Boolean(usuario?.permisos?.includes(permiso)), [usuario])

  return (
    <AuthContext.Provider value={{ usuario, cargando, login, logout, puede, recargar: cargarUsuario }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
