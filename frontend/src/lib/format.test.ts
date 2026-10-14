import { describe, expect, it } from 'vitest'
import { formatId, formatName, statLabel } from './format'

describe('format helpers', () => {
  it('formats slugs as display names', () => {
    expect(formatName('mr-mime')).toBe('Mr Mime')
    expect(formatName('pikachu')).toBe('Pikachu')
  })

  it('pads pokedex numbers', () => {
    expect(formatId(25)).toBe('#0025')
    expect(formatId(1025)).toBe('#1025')
  })

  it('labels known stats and falls back for unknown ones', () => {
    expect(statLabel('special-attack')).toBe('Sp. Atk')
    expect(statLabel('accuracy')).toBe('Accuracy')
  })
})
