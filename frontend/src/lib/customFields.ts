/**
 * Client-side mirror of the backend CustomFields rules, for instant feedback.
 * The server remains the source of truth and its field errors are shown as well.
 */
export const LIMITS = {
  localizedName: 100,
  region: 50,
  tags: 10,
  notes: 500,
} as const

const TAG_PATTERN = /^[a-z0-9][a-z0-9-]{0,29}$/

export interface CustomFieldsForm {
  localizedName: string
  region: string
  tags: string
  notes: string
}

export type FieldErrors = Partial<Record<keyof CustomFieldsForm, string>>

/** "Starter, gen-1, starter" -> ["starter", "gen-1"] */
export function parseTags(input: string): string[] {
  const tags = input
    .split(',')
    .map((tag) => tag.trim().toLowerCase())
    .filter(Boolean)
  return [...new Set(tags)]
}

export function validateCustomFields(form: CustomFieldsForm): FieldErrors {
  const errors: FieldErrors = {}
  const tags = parseTags(form.tags)

  if (form.localizedName.trim().length > LIMITS.localizedName) {
    errors.localizedName = `Must be at most ${LIMITS.localizedName} characters`
  }
  if (form.region.trim().length > LIMITS.region) {
    errors.region = `Must be at most ${LIMITS.region} characters`
  }
  if (tags.length > LIMITS.tags) {
    errors.tags = `Use at most ${LIMITS.tags} tags`
  } else if (!tags.every((tag) => TAG_PATTERN.test(tag))) {
    errors.tags = 'Tags may only contain letters, digits and hyphens (max 30 characters each)'
  }
  if (form.notes.trim().length > LIMITS.notes) {
    errors.notes = `Must be at most ${LIMITS.notes} characters`
  }
  return errors
}

const blankToNull = (value: string) => (value.trim() === '' ? null : value.trim())

export function toPayload(form: CustomFieldsForm, version: number) {
  return {
    localizedName: blankToNull(form.localizedName),
    region: blankToNull(form.region),
    tags: parseTags(form.tags),
    notes: blankToNull(form.notes),
    version,
  }
}
