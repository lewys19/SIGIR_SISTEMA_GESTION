import { describe, expect, it } from 'vitest'
import { validar } from './RegistroIncidente'

const completo = {
  titulo: 'Fuga', descripcion: 'Detalle', tipo: 'FRAUDE', severidad: 'ALTA', regulador: 'SBS', area: '',
  fechaOcurrencia: '2026-10-20', fechaDeteccion: '2026-10-21', impacto: '', medidas: '',
}

describe('validar el formulario de registro', () => {
  it('acepta un formulario completo', () => {
    expect(validar(completo, '2026-10-31')).toEqual({})
  })

  it('marca los obligatorios', () => {
    const e = validar({ ...completo, titulo: ' ', tipo: '' }, '2026-10-31')
    expect(Object.keys(e)).toEqual(['titulo', 'tipo'])
  })

  it('rechaza fechas futuras y detección antes de ocurrencia', () => {
    expect(validar({ ...completo, fechaOcurrencia: '2026-11-05', fechaDeteccion: '2026-11-05' }, '2026-10-31'))
      .toMatchObject({ fechaOcurrencia: 'No puede ser futura', fechaDeteccion: 'No puede ser futura' })
    expect(validar({ ...completo, fechaDeteccion: '2026-10-10' }, '2026-10-31').fechaDeteccion)
      .toBe('No puede ser anterior a la ocurrencia')
  })
})
