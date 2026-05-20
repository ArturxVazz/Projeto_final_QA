package com.library.library_manager.repository;

import com.library.library_manager.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername (String username);

    Optional<User> findByemail (String email);

    boolean existsByUsername (String username);

    boolean existsByEmail (String email);

}
