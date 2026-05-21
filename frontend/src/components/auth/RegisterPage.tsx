import { useState, FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import './Auth.css'

export default function RegisterPage() {
  const { register } = useAuth()
  const [form, setForm] = useState({ username: '', email: '', password: '', confirm: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError('')
    if (form.password !== form.confirm) { setError('Senhas não coincidem'); return }
    if (form.password.length < 6) { setError('Senha deve ter no mínimo 6 caracteres'); return }
    setLoading(true)
    try { await register({ username: form.username, email: form.email, password: form.password }) }
    catch (err: unknown) {
      const msg = (err as {response?: {data?: {message?: string}}})?.response?.data?.message
      setError(msg || 'Erro ao criar conta')
    }
    finally { setLoading(false) }
  }

  return (
    <div className="auth-page">
      <div className="auth-card card">
        <div className="auth-header">
          <div className="auth-logo">📚</div>
          <h1 className="auth-title">Criar Conta</h1>
          <p className="auth-subtitle">Comece sua biblioteca pessoal</p>
        </div>
        <form onSubmit={handleSubmit} className="auth-form">
          {error && <div className="auth-error">{error}</div>}
          {(['username','email','password','confirm'] as const).map((field) => (
            <div className="form-group" key={field}>
              <label className="label" htmlFor={field}>
                {field === 'username' ? 'Usuário' : field === 'email' ? 'Email' : field === 'password' ? 'Senha' : 'Confirmar Senha'}
              </label>
              <input id={field} className="input-field"
                type={field.includes('password') || field === 'confirm' ? 'password' : field === 'email' ? 'email' : 'text'}
                value={form[field]} onChange={e => setForm({...form, [field]: e.target.value})} required />
            </div>
          ))}
          <button type="submit" className="btn btn-primary auth-submit" disabled={loading}>
            {loading ? 'Criando...' : 'Criar Conta'}
          </button>
        </form>
        <p className="auth-link">Já tem conta? <Link to="/login">Entrar</Link></p>
      </div>
    </div>
  )
}
