package com.biblioteca.repository;
import com.biblioteca.model.Book;
import com.biblioteca.model.Book.ReadingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;
public interface BookRepository extends MongoRepository<Book, String> {
    List<Book> findByUserId(String userId);
    List<Book> findByUserIdAndStatus(String userId, ReadingStatus status);
    List<Book> findByUserIdAndGenre(String userId, String genre);
    Optional<Book> findByIsbn(String isbn);
    boolean existsByIsbnAndUserId(String isbn, String userId);
    List<Book> findByUserIdAndTitleContainingIgnoreCase(String userId, String title);
}
