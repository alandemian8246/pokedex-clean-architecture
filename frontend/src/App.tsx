import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Route, Routes } from 'react-router'
import { ApiError } from './api/http'
import { AuthProvider } from './auth/AuthProvider'
import { RequireAuth } from './auth/RequireAuth'
import { Layout } from './components/Layout'
import { AuthPage } from './pages/AuthPage'
import { CatalogPage } from './pages/CatalogPage'
import { EditLocalPokemonPage } from './pages/EditLocalPokemonPage'
import { MyPokedexPage } from './pages/MyPokedexPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { PokemonDetailPage } from './pages/PokemonDetailPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // client errors (4xx) will not succeed on retry; network and 5xx errors get two more attempts
      retry: (failureCount, error) =>
        !(error instanceof ApiError && error.status >= 400 && error.status < 500) && failureCount < 2,
      refetchOnWindowFocus: false,
    },
  },
})

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route element={<Layout />}>
              <Route index element={<CatalogPage />} />
              <Route path="pokemon/:idOrName" element={<PokemonDetailPage />} />
              <Route
                path="my-pokedex"
                element={
                  <RequireAuth>
                    <MyPokedexPage />
                  </RequireAuth>
                }
              />
              <Route
                path="my-pokedex/:id/edit"
                element={
                  <RequireAuth>
                    <EditLocalPokemonPage />
                  </RequireAuth>
                }
              />
              <Route path="login" element={<AuthPage mode="login" />} />
              <Route path="register" element={<AuthPage mode="register" />} />
              <Route path="*" element={<NotFoundPage />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  )
}
