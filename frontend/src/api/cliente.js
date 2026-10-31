const CLAVE_TOKEN = 'sigir.token'

export const tokenGuardado = () => sessionStorage.getItem(CLAVE_TOKEN)
export const guardarToken = (t) => sessionStorage.setItem(CLAVE_TOKEN, t)
export const borrarToken = () => sessionStorage.removeItem(CLAVE_TOKEN)

export class ErrorApi extends Error {
  constructor(estado, mensaje) {
    super(mensaje)
    this.estado = estado
  }
}

/** Arma "?a=1&b=2" ignorando valores vacíos. */
export function query(params = {}) {
  const q = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') q.set(k, v)
  })
  const s = q.toString()
  return s ? `?${s}` : ''
}

function cabeceras() {
  const h = { 'Content-Type': 'application/json' }
  const token = tokenGuardado()
  if (token) h.Authorization = `Bearer ${token}`
  return h
}

async function error(res) {
  const datos = await res.json().catch(() => null)
  if (res.status === 401 && tokenGuardado()) {
    window.dispatchEvent(new Event('sigir:sesion-expirada'))
  }
  const porDefecto = {
    400: 'Revisa los datos ingresados',
    403: 'No tienes permiso para esta acción',
    404: 'No encontrado',
  }[res.status] ?? 'Error del servidor'
  return new ErrorApi(res.status, datos?.mensaje ?? porDefecto)
}

/** fetch con JSON y el JWT en la cabecera Authorization. */
export async function api(ruta, { metodo = 'GET', cuerpo } = {}) {
  const res = await fetch(`/api${ruta}`, {
    method: metodo,
    headers: cabeceras(),
    body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined,
  })
  if (!res.ok) throw await error(res)
  if (res.status === 204) return null
  return res.json().catch(() => null)
}

/** Descarga un archivo protegido (el navegador no manda el JWT en un <a href>). */
export async function descargar(ruta, nombrePorDefecto) {
  const res = await fetch(`/api${ruta}`, { headers: cabeceras() })
  if (!res.ok) throw await error(res)
  const disp = res.headers.get('Content-Disposition') ?? ''
  const nombre = /filename="([^"]+)"/.exec(disp)?.[1] ?? nombrePorDefecto
  const url = URL.createObjectURL(await res.blob())
  const a = Object.assign(document.createElement('a'), { href: url, download: nombre })
  a.click()
  URL.revokeObjectURL(url)
}
