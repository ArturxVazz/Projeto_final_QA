package com.library.repository;

import com.library.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends MongoRepository<Book, String> {
    List<Book> findByUserId(String userId);
    List<Book> findByUserIdAndStatus(String userId, Book.ReadingStatus status);
    List<Book> findByUserIdAndTitleContainingIgnoreCase(String userId, String title);
    Optional<Book> findByIdAndUserId(String id, String userId);
    void deleteByIdAndUserId(String id, String userId);
    long countByUserId(String userId);
    long countByUserIdAndStatus(String userId, Book.ReadingStatus status);
}
