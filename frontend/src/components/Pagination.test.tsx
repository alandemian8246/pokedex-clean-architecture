import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { Pagination } from './Pagination'

describe('Pagination', () => {
  it('renders nothing for a single page', () => {
    const { container } = render(<Pagination page={0} totalPages={1} onPageChange={vi.fn()} />)
    expect(container).toBeEmptyDOMElement()
  })

  it('disables Previous on the first page and moves forward', async () => {
    const onPageChange = vi.fn()
    render(<Pagination page={0} totalPages={3} onPageChange={onPageChange} />)

    expect(screen.getByRole('button', { name: /previous/i })).toBeDisabled()
    expect(screen.getByText(/of 3/)).toHaveTextContent('Page 1 of 3')

    await userEvent.click(screen.getByRole('button', { name: /next/i }))
    expect(onPageChange).toHaveBeenCalledWith(1)
  })

  it('disables Next on the last page', () => {
    render(<Pagination page={2} totalPages={3} onPageChange={vi.fn()} />)
    expect(screen.getByRole('button', { name: /next/i })).toBeDisabled()
  })
})
