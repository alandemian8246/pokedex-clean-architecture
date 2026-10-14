import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { describe, expect, it } from 'vitest'
import type { Evolution } from '../api/types'
import { EvolutionTree } from './EvolutionTree'

const stage = (id: number, name: string, trigger: string | null, evolvesTo: Evolution[] = []): Evolution => ({
  id,
  name,
  spriteUrl: `${id}.png`,
  trigger,
  evolvesTo,
})

describe('EvolutionTree', () => {
  it('shows every branch with its trigger and highlights the current Pokemon', () => {
    const eevee = stage(133, 'eevee', null, [stage(134, 'vaporeon', 'Use water stone'), stage(135, 'jolteon', 'Use thunder stone')])

    render(
      <MemoryRouter>
        <EvolutionTree chain={eevee} currentId={135} />
      </MemoryRouter>,
    )

    expect(screen.getByText('Use water stone')).toBeInTheDocument()
    expect(screen.getByText('Use thunder stone')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /jolteon/i })).toHaveAttribute('aria-current', 'page')
    expect(screen.getByRole('link', { name: /vaporeon/i })).toHaveAttribute('href', '/pokemon/vaporeon')
  })

  it('explains when a Pokemon does not evolve', () => {
    render(
      <MemoryRouter>
        <EvolutionTree chain={stage(128, 'tauros', null)} currentId={128} />
      </MemoryRouter>,
    )
    expect(screen.getByText(/does not evolve/i)).toBeInTheDocument()
  })
})
