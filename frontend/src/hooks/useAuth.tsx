import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { authService, User, LoginRequest, RegisterRequest } from '../services/authService'

interface AuthContextType {
  user: User | null; isAuthenticated: boolean; isLoading: boolean
  login: (data: LoginRequest) => Promise<void>
  register: (data: RegisterRequest) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (token) {
      authService.me().then(setUser).catch(() => localStorage.removeItem('token')).finally(() => setIsLoading(false))
    } else { setIsLoading(false) }
  }, [])

  const login = async (data: LoginRequest) => {
    const res = await authService.login(data)
    localStorage.setItem('token', res.token)
    setUser(await authService.me())
  }

  const register = async (data: RegisterRequest) => {
    await authService.register(data)
    await login({ username: data.username, password: data.password })
  }

  const logout = () => { localStorage.removeItem('token'); setUser(null); window.location.href = '/login' }

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
