import { describe, expect, it } from 'vitest'
import { diasHasta, etiqueta, fecha, textoPlazo } from './formato'
import { query } from '../api/cliente'

const hoy = new Date(2026, 9, 31) // 31/10/2026

describe('formato', () => {
  it('muestra fechas ISO en formato peruano sin correrse de día', () => {
    expect(fecha('2026-01-05')).toBe('05/01/2026')
    expect(fecha(null)).toBe('—')
  })

  it('calcula los días que faltan para el plazo', () => {
    expect(diasHasta('2026-11-03', hoy)).toBe(3)
    expect(diasHasta('2026-10-31', hoy)).toBe(0)
    expect(diasHasta('2026-10-29', hoy)).toBe(-2)
  })

  it('describe el plazo en palabras', () => {
    expect(textoPlazo('2026-11-01', hoy)).toBe('Faltan 1 día')
    expect(textoPlazo('2026-10-31', hoy)).toBe('Vence hoy')
    expect(textoPlazo('2026-10-28', hoy)).toBe('Vencido hace 3 días')
  })

  it('traduce los códigos del backend', () => {
    expect(etiqueta('EN_REVISION')).toBe('En revisión')
    expect(etiqueta('DESCONOCIDO')).toBe('DESCONOCIDO')
  })
})

describe('query', () => {
  it('omite los filtros vacíos', () => {
    expect(query({ estado: 'BORRADOR', q: '', severidad: null, pagina: 0 })).toBe('?estado=BORRADOR&pagina=0')
    expect(query({})).toBe('')
  })
})
