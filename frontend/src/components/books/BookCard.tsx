import { Book, ReadingStatus } from '../../services/bookService'
import './Books.css'

const STATUS_LABELS: Record<ReadingStatus, string> = {
  TO_READ: 'Para Ler', READING: 'Lendo', COMPLETED: 'Concluído', ABANDONED: 'Abandonado'
}
const STATUS_CLASS: Record<ReadingStatus, string> = {
  TO_READ: 'to-read', READING: 'reading', COMPLETED: 'completed', ABANDONED: 'abandoned'
}

interface Props { book: Book; onEdit: (b: Book) => void; onDelete: (id: string) => void }

export default function BookCard({ book, onEdit, onDelete }: Props) {
  return (
    <div className="book-card card">
      <div className="book-card-header">
        <div className="book-spine" style={{ background: stringToColor(book.title) }} />
        <div className="book-card-info">
          <h3 className="book-title">{book.title}</h3>
          <p className="book-author">{book.author}</p>
          {book.genre && <p className="book-genre">{book.genre}</p>}
        </div>
      </div>
      <div className="book-card-body">
        {book.description && <p className="book-description">{book.description.slice(0, 100)}{book.description.length > 100 ? '...' : ''}</p>}
        <div className="book-meta">
          {book.year && <span className="book-year">{book.year}</span>}
          {book.isbn && <span className="book-isbn">ISBN: {book.isbn}</span>}
        </div>
      </div>
      <div className="book-card-footer">
        <span className={`badge badge-${STATUS_CLASS[book.status]}`}>{STATUS_LABELS[book.status]}</span>
        {book.rating && <span className="book-rating">{'★'.repeat(book.rating)}{'☆'.repeat(5 - book.rating)}</span>}
        <div className="book-actions">
          <button className="btn btn-ghost" onClick={() => onEdit(book)}>✏️ Editar</button>
          <button className="btn btn-danger" onClick={() => onDelete(book.id)}>🗑️</button>
        </div>
      </div>
    </div>
  )
}

function stringToColor(str: string): string {
  let hash = 0
  for (let i = 0; i < str.length; i++) hash = str.charCodeAt(i) + ((hash << 5) - hash)
  const colors = ['#2d5a8e','#8e2d5a','#5a8e2d','#8e5a2d','#2d8e7a','#7a2d8e','#8e7a2d','#2d3d8e']
  return colors[Math.abs(hash) % colors.length]
}
