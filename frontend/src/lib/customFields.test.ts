import { describe, expect, it } from 'vitest'
import { parseTags, toPayload, validateCustomFields, type CustomFieldsForm } from './customFields'

const valid: CustomFieldsForm = { localizedName: 'Bisasam', region: 'Kanto', tags: 'starter, gen-1', notes: '' }

describe('parseTags', () => {
  it('splits, trims, lowercases and de-duplicates', () => {
    expect(parseTags(' Starter, gen-1 ,starter,, ')).toEqual(['starter', 'gen-1'])
  })
})

describe('validateCustomFields', () => {
  it('accepts valid input', () => {
    expect(validateCustomFields(valid)).toEqual({})
  })

  it('flags values that exceed the server limits', () => {
    const errors = validateCustomFields({
      localizedName: 'x'.repeat(101),
      region: 'y'.repeat(51),
      tags: '',
      notes: 'n'.repeat(501),
    })
    expect(Object.keys(errors)).toEqual(['localizedName', 'region', 'notes'])
  })

  it('rejects malformed and too many tags', () => {
    expect(validateCustomFields({ ...valid, tags: 'has space' }).tags).toMatch(/letters, digits and hyphens/)
    const eleven = Array.from({ length: 11 }, (_, i) => `t${i}`).join(',')
    expect(validateCustomFields({ ...valid, tags: eleven }).tags).toMatch(/at most 10/)
  })
})

describe('toPayload', () => {
  it('turns blank strings into null and keeps the version', () => {
    expect(toPayload({ localizedName: ' ', region: 'Kanto ', tags: 'A', notes: '' }, 3)).toEqual({
      localizedName: null,
      region: 'Kanto',
      tags: ['a'],
      notes: null,
      version: 3,
    })
  })
})
