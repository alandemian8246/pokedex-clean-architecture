import { Link, NavLink, Outlet, useNavigate } from 'react-router'
import { useAuth } from '../auth/useAuth'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-lg px-3 py-2 text-sm font-semibold transition-colors ${
    isActive ? 'bg-white/20 text-white' : 'text-red-100 hover:bg-white/10 hover:text-white'
  }`

export function Layout() {
  const { user, isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-10 bg-brand-600 shadow">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-x-6 gap-y-2 px-4 py-3">
          <Link to="/" className="flex items-center gap-2 text-xl font-extrabold text-white">
            <span aria-hidden="true" className="relative h-7 w-7 rounded-full border-[3px] border-slate-900 bg-white">
              <span className="absolute inset-x-0 top-0 h-1/2 rounded-t-full bg-brand-500" />
              <span className="absolute left-1/2 top-1/2 h-2.5 w-2.5 -translate-x-1/2 -translate-y-1/2 rounded-full border-2 border-slate-900 bg-white" />
            </span>
            Pokédex
          </Link>
          <nav aria-label="Main" className="flex gap-1">
            <NavLink to="/" end className={navLinkClass}>
              Catalog
            </NavLink>
            <NavLink to="/my-pokedex" className={navLinkClass}>
              My Pokédex
            </NavLink>
          </nav>
          <div className="ml-auto flex items-center gap-3 text-sm">
            {isAuthenticated && user ? (
              <>
                <span className="text-red-50">
                  <span className="font-semibold">{user.username}</span>
                  {user.role === 'ADMIN' && (
                    <span className="ml-2 rounded bg-slate-900 px-1.5 py-0.5 text-xs font-bold text-white">ADMIN</span>
                  )}
                </span>
                <button type="button" onClick={handleLogout} className="rounded-lg bg-white/15 px-3 py-1.5 font-semibold text-white hover:bg-white/25">
                  Log out
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="font-semibold text-white hover:underline">
                  Log in
                </Link>
                <Link to="/register" className="rounded-lg bg-white px-3 py-1.5 font-semibold text-brand-700 hover:bg-red-50">
                  Sign up
                </Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6">
        <Outlet />
      </main>
      <footer className="border-t border-slate-200 py-4 text-center text-xs text-slate-500">
        Data from{' '}
        <a href="https://pokeapi.co" target="_blank" rel="noreferrer" className="underline">
          PokeAPI
        </a>
      </footer>
    </div>
  )
}
