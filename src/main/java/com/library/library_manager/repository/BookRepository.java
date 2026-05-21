package com.library.library_manager.repository;


import com.library.library_manager.model.Book;
import com.library.library_manager.model.ReadingStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository {

    List<Book> findByUserId(String userId);

    List<Book> findByUserIdAndStatus(String userId, ReadingStatus status);

    List<Book> findByUserIdAndTitleContainingIgnoreCase(String userId, String title);

    Optional<Book> findByIdAndUserId(String id, String userId);

    long countByUserId(String userId);
    long countByUserIdAndStatus(String userId, ReadingStatus status);
}

