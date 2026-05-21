import { ReactNode } from 'react'
import { useAuth } from '../../hooks/useAuth'
import './Layout.css'

export default function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth()
  return (
    <div className="layout">
      <header className="header">
        <div className="header-inner">
          <div className="header-brand">
            <span className="header-icon">📚</span>
            <span className="header-title">Biblioteca Pessoal</span>
          </div>
          <div className="header-user">
            <span className="header-username">Olá, {user?.username}</span>
            <button className="btn btn-ghost" onClick={logout}>Sair</button>
          </div>
        </div>
      </header>
      <main className="main-content">{children}</main>
      <footer className="footer">
        <span>Biblioteca Pessoal © 2024 — Gerenciamento de livros com Spring Boot + MongoDB</span>
      </footer>
    </div>
  )
}
