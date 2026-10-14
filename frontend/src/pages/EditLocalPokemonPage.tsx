import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { ApiError } from '../api/http'
import { useLocalPokemon, useUpdateLocalPokemon } from '../api/queries'
import type { LocalPokemon } from '../api/types'
import { EmptyState, ErrorMessage, Spinner } from '../components/Feedback'
import { Sprite } from '../components/Sprite'
import {
  LIMITS,
  toPayload,
  validateCustomFields,
  type CustomFieldsForm,
  type FieldErrors,
} from '../lib/customFields'
import { formatId, formatName } from '../lib/format'

/** User Story 04: edit the proprietary fields of a stored Pokemon. */
export function EditLocalPokemonPage() {
  const id = Number(useParams().id)
  const validId = Number.isInteger(id) && id > 0
  const query = useLocalPokemon(id, validId)

  if (!validId) return <NotStored />
  if (query.isPending) return <Spinner label="Loading" />
  if (query.isError) {
    if (query.error instanceof ApiError && query.error.status === 404) return <NotStored />
    return <ErrorMessage error={query.error} onRetry={() => query.refetch()} />
  }
  // keyed by version: reloading after a conflict re-initializes the form with the latest data
  return <EditForm key={query.data.version} pokemon={query.data} onReload={() => query.refetch()} />
}

function NotStored() {
  return (
    <EmptyState title="This Pokémon is not in your Pokédex">
      <Link to="/my-pokedex" className="btn-primary">
        Back to my Pokédex
      </Link>
    </EmptyState>
  )
}

function toForm(pokemon: LocalPokemon): CustomFieldsForm {
  return {
    localizedName: pokemon.localizedName ?? '',
    region: pokemon.region ?? '',
    tags: pokemon.tags.join(', '),
    notes: pokemon.notes ?? '',
  }
}

function EditForm({ pokemon, onReload }: { pokemon: LocalPokemon; onReload: () => void }) {
  const navigate = useNavigate()
  const update = useUpdateLocalPokemon(pokemon.id)
  const [form, setForm] = useState<CustomFieldsForm>(() => toForm(pokemon))
  const [errors, setErrors] = useState<FieldErrors>({})

  const isConflict = update.error instanceof ApiError && update.error.status === 409

  const setField = (field: keyof CustomFieldsForm) => (value: string) => {
    setForm((current) => ({ ...current, [field]: value }))
    setErrors((current) => ({ ...current, [field]: undefined }))
  }

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    const clientErrors = validateCustomFields(form)
    setErrors(clientErrors)
    if (Object.keys(clientErrors).length > 0) return

    update.mutate(toPayload(form, pokemon.version), {
      onSuccess: () => navigate('/my-pokedex'),
      onError: (error) => {
        if (error instanceof ApiError) setErrors(error.fieldErrors as FieldErrors)
      },
    })
  }

  return (
    <section className="mx-auto flex max-w-2xl flex-col gap-6">
      <Link to="/my-pokedex" className="text-sm font-semibold text-brand-600 hover:underline">
        ← Back to my Pokédex
      </Link>

      <header className="card flex items-center gap-4 p-4">
        <Sprite src={pokemon.spriteUrl} alt={formatName(pokemon.name)} size={80} />
        <div>
          <p className="text-xs font-semibold text-slate-400">{formatId(pokemon.id)}</p>
          <h1 className="text-2xl font-extrabold">Edit {formatName(pokemon.name)}</h1>
          <p className="text-sm text-slate-500">
            Core data comes from PokeAPI; the fields below belong to your Pokédex.
          </p>
        </div>
      </header>

      {isConflict && (
        <div role="alert" className="card flex flex-col gap-3 border-amber-300 bg-amber-50 p-4 sm:flex-row sm:items-center">
          <p className="flex-1 text-sm text-amber-900">
            Someone else saved this Pokémon while you were editing. Reload to get the latest version, then reapply your
            changes.
          </p>
          <button type="button" className="btn-secondary" onClick={onReload}>
            Reload latest
          </button>
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate className="card flex flex-col gap-5 p-6">
        <TextField
          id="localizedName"
          label="Localized name"
          hint="e.g. the Japanese or German name"
          value={form.localizedName}
          onChange={setField('localizedName')}
          error={errors.localizedName}
        />
        <TextField
          id="region"
          label="Region"
          hint="Geographical metadata, e.g. Kanto or Viridian Forest"
          value={form.region}
          onChange={setField('region')}
          error={errors.region}
        />
        <TextField
          id="tags"
          label="Tags"
          hint={`Comma separated, up to ${LIMITS.tags}: lowercase letters, digits and hyphens`}
          value={form.tags}
          onChange={setField('tags')}
          error={errors.tags}
        />
        <div className="flex flex-col gap-1">
          <label htmlFor="notes" className="text-sm font-semibold">
            Notes
          </label>
          <textarea
            id="notes"
            rows={4}
            className="input"
            value={form.notes}
            onChange={(event) => setField('notes')(event.target.value)}
            aria-invalid={Boolean(errors.notes)}
            aria-describedby="notes-help"
          />
          <p id="notes-help" className={`text-xs ${errors.notes ? 'text-brand-700' : 'text-slate-500'}`}>
            {errors.notes ?? `${form.notes.length}/${LIMITS.notes} characters`}
          </p>
        </div>

        {update.isError && !isConflict && Object.keys(errors).length === 0 && (
          <p role="alert" className="text-sm text-brand-700">
            {update.error.message}
          </p>
        )}

        <div className="flex flex-wrap justify-end gap-2">
          <Link to="/my-pokedex" className="btn-secondary">
            Cancel
          </Link>
          <button type="submit" className="btn-primary" disabled={update.isPending || isConflict}>
            {update.isPending ? 'Saving…' : 'Save changes'}
          </button>
        </div>
      </form>
    </section>
  )
}

interface TextFieldProps {
  id: string
  label: string
  hint: string
  value: string
  onChange: (value: string) => void
  error?: string
}

function TextField({ id, label, hint, value, onChange, error }: TextFieldProps) {
  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={id} className="text-sm font-semibold">
        {label}
      </label>
      <input
        id={id}
        className="input"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        aria-invalid={Boolean(error)}
        aria-describedby={`${id}-help`}
      />
      <p id={`${id}-help`} className={`text-xs ${error ? 'text-brand-700' : 'text-slate-500'}`}>
        {error ?? hint}
      </p>
    </div>
  )
}
