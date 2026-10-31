const ETIQUETAS = {
  // tipos
  PROTECCION_DATOS: 'Protección de datos', CIBERSEGURIDAD: 'Ciberseguridad', LAVADO_ACTIVOS: 'Lavado de activos',
  FRAUDE: 'Fraude', OPERACIONAL: 'Operacional', CUMPLIMIENTO_NORMATIVO: 'Cumplimiento normativo',
  CONTINUIDAD_NEGOCIO: 'Continuidad de negocio', OTRO: 'Otro',
  // severidades
  BAJA: 'Baja', MEDIA: 'Media', ALTA: 'Alta', CRITICA: 'Crítica',
  // estados
  BORRADOR: 'Borrador', EN_REVISION: 'En revisión', APROBADO: 'Aprobado', RECHAZADO: 'Rechazado', CERRADO: 'Cerrado',
  ENVIADO: 'Enviado a revisión',
  // roles
  ADMIN: 'Administrador', SUPERVISOR: 'Supervisor', ANALISTA: 'Analista', AUDITOR: 'Auditor',
  // eventos
  INCIDENTE_CREADO: 'Incidente creado', INCIDENTE_ACTUALIZADO: 'Incidente actualizado',
  INCIDENTE_ENVIADO: 'Enviado a revisión', INCIDENTE_APROBADO: 'Incidente aprobado',
  INCIDENTE_RECHAZADO: 'Incidente rechazado', INCIDENTE_CERRADO: 'Incidente cerrado',
}

export const etiqueta = (v) => ETIQUETAS[v] ?? v ?? '—'

const PERMISOS = {
  'dashboard:ver': 'Ver dashboard', 'incidentes:ver': 'Ver incidentes', 'incidentes:crear': 'Registrar incidentes',
  'incidentes:editar': 'Editar y enviar incidentes', 'revision:aprobar': 'Aprobar / rechazar / cerrar',
  'monitoreo:ver': 'Monitoreo en vivo', 'reportes:ver': 'Reportes y exportación',
  'usuarios:gestionar': 'Gestionar usuarios', 'roles:gestionar': 'Gestionar roles y permisos',
  'auditoria:ver': 'Ver auditoría',
}
export const etiquetaPermiso = (p) => PERMISOS[p] ?? p

/** "2026-10-31" → "31/10/2026" sin desfases de zona horaria. */
export function fecha(iso) {
  if (!iso) return '—'
  const [a, m, d] = iso.slice(0, 10).split('-')
  return `${d}/${m}/${a}`
}

export function fechaHora(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('es-PE', { dateStyle: 'short', timeStyle: 'short' })
}

/** Días que faltan (negativo = vencido) hasta una fecha ISO. */
export function diasHasta(iso, hoy = new Date()) {
  const [a, m, d] = iso.split('-').map(Number)
  const objetivo = Date.UTC(a, m - 1, d)
  const base = Date.UTC(hoy.getFullYear(), hoy.getMonth(), hoy.getDate())
  return Math.round((objetivo - base) / 86400000)
}

export function textoPlazo(iso, hoy) {
  const n = diasHasta(iso, hoy)
  if (n < 0) return `Vencido hace ${-n} día${n === -1 ? '' : 's'}`
  if (n === 0) return 'Vence hoy'
  return `Faltan ${n} día${n === 1 ? '' : 's'}`
}

export const hoyIso = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
