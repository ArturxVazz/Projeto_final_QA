import { useState, FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import './Auth.css'

export default function LoginPage() {
  const { login } = useAuth()
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(''); setLoading(true)
    try { await login(form) }
    catch { setError('Usuário ou senha inválidos') }
    finally { setLoading(false) }
  }

  return (
    <div className="auth-page">
      <div className="auth-card card">
        <div className="auth-header">
          <div className="auth-logo">📚</div>
          <h1 className="auth-title">Biblioteca Pessoal</h1>
          <p className="auth-subtitle">Entre na sua conta</p>
        </div>
        <form onSubmit={handleSubmit} className="auth-form">
          {error && <div className="auth-error">{error}</div>}
          <div className="form-group">
            <label className="label" htmlFor="username">Usuário</label>
            <input id="username" className="input-field" type="text" placeholder="seu_usuario"
              value={form.username} onChange={e => setForm({...form, username: e.target.value})} required />
          </div>
          <div className="form-group">
            <label className="label" htmlFor="password">Senha</label>
            <input id="password" className="input-field" type="password" placeholder="••••••••"
              value={form.password} onChange={e => setForm({...form, password: e.target.value})} required />
          </div>
          <button type="submit" className="btn btn-primary auth-submit" disabled={loading}>
            {loading ? 'Entrando...' : 'Entrar'}
          </button>
        </form>
        <p className="auth-link">Não tem conta? <Link to="/register">Cadastre-se</Link></p>
      </div>
    </div>
  )
}
