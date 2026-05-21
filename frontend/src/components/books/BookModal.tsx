import { useState, FormEvent, useEffect } from 'react'
import { Book, BookRequest, ReadingStatus } from '../../services/bookService'
import './Books.css'

const STATUS_LABELS: Record<ReadingStatus, string> = {
  TO_READ: 'Para Ler', READING: 'Lendo', COMPLETED: 'Concluído', ABANDONED: 'Abandonado'
}

interface Props { book: Book | null; onSave: (data: BookRequest) => Promise<void>; onClose: () => void }

const empty: BookRequest = { title: '', author: '', isbn: '', genre: '', description: '', status: 'TO_READ', year: undefined, rating: undefined }

export default function BookModal({ book, onSave, onClose }: Props) {
  const [form, setForm] = useState<BookRequest>(empty)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (book) setForm({ title: book.title, author: book.author, isbn: book.isbn || '', genre: book.genre || '', description: book.description || '', status: book.status, year: book.year, rating: book.rating })
    else setForm(empty)
  }, [book])

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault(); setError(''); setLoading(true)
    try { await onSave(form) }
    catch (err: unknown) {
      const msg = (err as {response?: {data?: {message?: string}}})?.response?.data?.message
      setError(msg || 'Erro ao salvar livro')
    }
    finally { setLoading(false) }
  }

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal card">
        <div className="modal-header">
          <h2 className="modal-title">{book ? 'Editar Livro' : 'Novo Livro'}</h2>
          <button className="btn btn-ghost modal-close" onClick={onClose}>✕</button>
        </div>
        <form onSubmit={handleSubmit} className="modal-form">
          {error && <div className="auth-error">{error}</div>}
          <div className="modal-grid">
            <div className="form-group modal-full">
              <label className="label">Título *</label>
              <input className="input-field" value={form.title} onChange={e => setForm({...form, title: e.target.value})} required />
            </div>
            <div className="form-group modal-full">
              <label className="label">Autor *</label>
              <input className="input-field" value={form.author} onChange={e => setForm({...form, author: e.target.value})} required />
            </div>
            <div className="form-group">
              <label className="label">ISBN</label>
              <input className="input-field" value={form.isbn || ''} onChange={e => setForm({...form, isbn: e.target.value})} placeholder="978-..." />
            </div>
            <div className="form-group">
              <label className="label">Gênero</label>
              <input className="input-field" value={form.genre || ''} onChange={e => setForm({...form, genre: e.target.value})} placeholder="Romance, Ficção..." />
            </div>
            <div className="form-group">
              <label className="label">Ano</label>
              <input className="input-field" type="number" min={1000} max={2100} value={form.year || ''} onChange={e => setForm({...form, year: e.target.value ? Number(e.target.value) : undefined})} />
            </div>
            <div className="form-group">
              <label className="label">Status</label>
              <select className="input-field" value={form.status} onChange={e => setForm({...form, status: e.target.value as ReadingStatus})}>
                {Object.entries(STATUS_LABELS).map(([k,v]) => <option key={k} value={k}>{v}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="label">Avaliação (1-5)</label>
              <select className="input-field" value={form.rating || ''} onChange={e => setForm({...form, rating: e.target.value ? Number(e.target.value) : undefined})}>
                <option value="">Sem avaliação</option>
                {[1,2,3,4,5].map(n => <option key={n} value={n}>{'★'.repeat(n)} ({n})</option>)}
              </select>
            </div>
            <div className="form-group modal-full">
              <label className="label">Descrição</label>
              <textarea className="input-field" rows={3} value={form.description || ''} onChange={e => setForm({...form, description: e.target.value})} placeholder="Uma breve sinopse..." />
            </div>
          </div>
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancelar</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Salvando...' : book ? 'Atualizar' : 'Adicionar'}</button>
          </div>
        </form>
      </div>
    </div>
  )
}
