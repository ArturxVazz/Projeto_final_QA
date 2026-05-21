package com.biblioteca.controller;

import com.biblioteca.dto.request.BookRequest;
import com.biblioteca.dto.response.BookResponse;
import com.biblioteca.model.Book.ReadingStatus;
import com.biblioteca.repository.UserRepository;
import com.biblioteca.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Livros", description = "CRUD de livros da biblioteca pessoal")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private final BookService bookService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Adiciona novo livro")
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        String userId = getUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request, userId));
    }

    @GetMapping
    @Operation(summary = "Lista todos os livros do usuário")
    public ResponseEntity<List<BookResponse>> findAll(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookService.findAllByUser(getUserId(userDetails)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca livro por ID")
    public ResponseEntity<BookResponse> findById(@PathVariable String id,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookService.findById(id, getUserId(userDetails)));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Filtra livros por status de leitura")
    public ResponseEntity<List<BookResponse>> findByStatus(@PathVariable ReadingStatus status,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookService.findByStatus(getUserId(userDetails), status));
    }

    @GetMapping("/search")
    @Operation(summary = "Busca livros por título")
    public ResponseEntity<List<BookResponse>> search(@RequestParam String title,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookService.searchByTitle(getUserId(userDetails), title));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza livro")
    public ResponseEntity<BookResponse> update(@PathVariable String id,
                                               @Valid @RequestBody BookRequest request,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookService.update(id, request, getUserId(userDetails)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove livro")
    public ResponseEntity<Void> delete(@PathVariable String id,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        bookService.delete(id, getUserId(userDetails));
        return ResponseEntity.noContent().build();
    }

    private String getUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow().getId();
    }
}
