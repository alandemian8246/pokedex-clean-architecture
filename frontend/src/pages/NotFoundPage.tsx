import { Link } from 'react-router'
import { EmptyState } from '../components/Feedback'

export function NotFoundPage() {
  return (
    <EmptyState title="Page not found">
      <Link to="/" className="btn-primary">
        Go to the catalog
      </Link>
    </EmptyState>
  )
}
