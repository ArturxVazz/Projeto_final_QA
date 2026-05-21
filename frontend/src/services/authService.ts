import api from './api'

export interface LoginRequest { username: string; password: string }
export interface RegisterRequest { username: string; email: string; password: string }
export interface AuthResponse { token: string; type: string; username: string; email: string }
export interface User { id: string; username: string; email: string; createdAt: string }

export const authService = {
  async login(data: LoginRequest): Promise<AuthResponse> {
    return (await api.post<AuthResponse>('/auth/login', data)).data
  },
  async register(data: RegisterRequest): Promise<User> {
    return (await api.post<User>('/auth/register', data)).data
  },
  async me(): Promise<User> {
    return (await api.get<User>('/users/me')).data
  }
}
