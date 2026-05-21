import api from './api'

export type ReadingStatus = 'TO_READ' | 'READING' | 'COMPLETED' | 'ABANDONED'

export interface Book {
  id: string; title: string; author: string; isbn?: string; genre?: string
  year?: number; description?: string; status: ReadingStatus; rating?: number
  createdAt: string; updatedAt?: string
}

export interface BookRequest {
  title: string; author: string; isbn?: string; genre?: string
  year?: number; description?: string; status?: ReadingStatus; rating?: number
}

export const bookService = {
  async list(): Promise<Book[]> { return (await api.get<Book[]>('/books')).data },
  async get(id: string): Promise<Book> { return (await api.get<Book>(`/books/${id}`)).data },
  async create(data: BookRequest): Promise<Book> { return (await api.post<Book>('/books', data)).data },
  async update(id: string, data: BookRequest): Promise<Book> { return (await api.put<Book>(`/books/${id}`, data)).data },
  async delete(id: string): Promise<void> { await api.delete(`/books/${id}`) },
  async search(title: string): Promise<Book[]> { return (await api.get<Book[]>(`/books/search?title=${encodeURIComponent(title)}`)).data },
  async filterByStatus(status: ReadingStatus): Promise<Book[]> { return (await api.get<Book[]>(`/books/status/${status}`)).data }
}
