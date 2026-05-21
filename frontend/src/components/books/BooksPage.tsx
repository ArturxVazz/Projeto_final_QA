import { useState, useEffect, useCallback } from 'react'
import { bookService, Book, BookRequest, ReadingStatus } from '../../services/bookService'
import BookCard from './BookCard'
import BookModal from './BookModal'
import './Books.css'

const STATUS_LABELS: Record<ReadingStatus, string> = {
  TO_READ: 'Para Ler', READING: 'Lendo', COMPLETED: 'Lidos', ABANDONED: 'Abandonados'
}

export default function BooksPage() {
  const [books, setBooks] = useState<Book[]>([])
  const [filtered, setFiltered] = useState<Book[]>([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<ReadingStatus | 'ALL'>('ALL')
  const [modalOpen, setModalOpen] = useState(false)
  const [editBook, setEditBook] = useState<Book | null>(null)

  const load = useCallback(async () => {
    try {
      const data = await bookService.list()
      setBooks(data)
    } finally { setLoading(false) }
  }, [])

  useEffect(() => { load() }, [load])

  useEffect(() => {
    let result = books
    if (statusFilter !== 'ALL') result = result.filter(b => b.status === statusFilter)
    if (search.trim()) result = result.filter(b => b.title.toLowerCase().includes(search.toLowerCase()) || b.author.toLowerCase().includes(search.toLowerCase()))
    setFiltered(result)
  }, [books, search, statusFilter])

  const handleSave = async (data: BookRequest) => {
    if (editBook) await bookService.update(editBook.id, data)
    else await bookService.create(data)
    await load()
    setModalOpen(false); setEditBook(null)
  }

  const handleDelete = async (id: string) => {
    if (!confirm('Tem certeza que deseja excluir este livro?')) return
    await bookService.delete(id)
    setBooks(prev => prev.filter(b => b.id !== id))
  }

  const handleEdit = (book: Book) => { setEditBook(book); setModalOpen(true) }

  const stats = {
    total: books.length,
    reading: books.filter(b => b.status === 'READING').length,
    completed: books.filter(b => b.status === 'COMPLETED').length,
    toRead: books.filter(b => b.status === 'TO_READ').length,
  }

  if (loading) return <div className="books-loading">Carregando sua biblioteca...</div>

  return (
    <div className="books-page">
      <div className="books-hero">
        <div>
          <h1 className="books-hero-title">Minha Biblioteca</h1>
          <p className="books-hero-sub">{stats.total} livros · {stats.reading} lendo · {stats.completed} concluídos</p>
        </div>
        <button className="btn btn-primary" onClick={() => { setEditBook(null); setModalOpen(true) }}>
          + Adicionar Livro
        </button>
      </div>

      <div className="books-stats">
        {Object.entries(STATUS_LABELS).map(([key, label]) => (
          <button key={key} className={`stat-card ${statusFilter === key ? 'active' : ''}`}
            onClick={() => setStatusFilter(statusFilter === key ? 'ALL' : key as ReadingStatus)}>
            <span className="stat-num">{books.filter(b => b.status === key).length}</span>
            <span className="stat-label">{label}</span>
          </button>
        ))}
      </div>

      <div className="books-filters">
        <input className="input-field books-search" type="search" placeholder="Buscar por título ou autor..."
          value={search} onChange={e => setSearch(e.target.value)} />
        <select className="input-field books-select" value={statusFilter}
          onChange={e => setStatusFilter(e.target.value as ReadingStatus | 'ALL')}>
          <option value="ALL">Todos os Status</option>
          {Object.entries(STATUS_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
      </div>

      {filtered.length === 0 ? (
        <div className="books-empty">
          <div className="books-empty-icon">📖</div>
          <h3>{books.length === 0 ? 'Sua biblioteca está vazia' : 'Nenhum livro encontrado'}</h3>
          <p>{books.length === 0 ? 'Adicione seu primeiro livro para começar!' : 'Tente ajustar os filtros de busca.'}</p>
          {books.length === 0 && <button className="btn btn-primary" onClick={() => setModalOpen(true)}>Adicionar Primeiro Livro</button>}
        </div>
      ) : (
        <div className="books-grid">
          {filtered.map(book => (
            <BookCard key={book.id} book={book} onEdit={handleEdit} onDelete={handleDelete} />
          ))}
        </div>
      )}

      {modalOpen && (
        <BookModal book={editBook} onSave={handleSave} onClose={() => { setModalOpen(false); setEditBook(null) }} />
      )}
    </div>
  )
}
