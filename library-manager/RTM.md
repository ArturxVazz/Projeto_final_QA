# RTM.md — Matriz de Rastreabilidade de Requisitos

> **Projeto:** Gerenciador de Biblioteca Pessoal  
> **Versão:** 1.0.0  
> **Cobertura de Requisitos:** 7/7 (100%)

---

## Índice de Requisitos

| ID | Requisito | Status | Testes |
|---|---|---|---|
| RF01 | Cadastro de usuário | ✅ | AuthControllerIT, UserServiceTest |
| RF02 | Autenticação de usuário (login) | ✅ | AuthControllerIT, UserServiceTest, WhiteBoxTest |
| RF03 | Criação de livro | ✅ | BookControllerIT, BookServiceTest, BookServiceParameterizedTest |
| RF04 | Listagem/busca de livros | ✅ | BookControllerIT, BookServiceTest, BookServiceParameterizedTest |
| RF05 | Atualização de livro | ✅ | BookControllerIT, BookServiceTest |
| RF06 | Remoção de livro | ✅ | BookControllerIT, BookServiceTest |
| RF07 | Estatísticas da biblioteca | ✅ | BookControllerIT, BookServiceTest, BookServiceParameterizedTest |
| RF08 | Busca de livros na Open Library (API externa) | ✅ | OpenLibraryServiceVCRTest, BookControllerIT |

---

## RF01 — Cadastro de Usuário

**Descrição:** O sistema deve permitir que um novo usuário se cadastre informando username, email e senha. A senha deve ser armazenada com hash BCrypt. Um token JWT deve ser retornado.

**Regras de Negócio:**
- Username e email devem ser únicos
- Senha mínima: 6 caracteres
- Username: 3–50 caracteres
- Email deve ser válido

**Testes Relacionados:**

| Teste | Classe | Tipo | Cenário |
|---|---|---|---|
| `shouldRegisterNewUserSuccessfully` | `AuthControllerIT` | Caixa Preta / E2E | Cadastro válido retorna 201 + JWT |
| `shouldReturn409WhenUsernameAlreadyExists` | `AuthControllerIT` | Caixa Preta | Username duplicado retorna 409 |
| `shouldReturn409WhenEmailAlreadyExists` | `AuthControllerIT` | Caixa Preta | Email duplicado retorna 409 |
| `shouldReturn400WhenRegistrationDataIsInvalid` | `AuthControllerIT` | Caixa Preta | Dados inválidos retornam 400 |
| `shouldRegisterNewUserAndReturnValidToken` | `UserServiceTest` | Integração (TC) | JWT gerado e persistência verificada |
| `shouldHashPasswordBeforeSaving` | `UserServiceTest` | Integração (TC) | Senha armazenada como BCrypt hash |
| `shouldThrowExceptionWhenUsernameIsDuplicated` | `UserServiceTest` | Integração (TC) | Exceção em username duplicado |
| `shouldThrowExceptionWhenEmailIsDuplicated` | `UserServiceTest` | Integração (TC) | Exceção em email duplicado |
| `shouldPersistCreationDateAutomatically` | `UserServiceTest` | Integração (TC) | createdAt preenchido automaticamente |

**Diagrama UML de Sequência — RF01:**

```
Cliente          AuthController        UserService         UserRepository       MongoDB
   │                   │                    │                    │                 │
   │ POST /register    │                    │                    │                 │
   │──────────────────>│                    │                    │                 │
   │                   │ register(request)  │                    │                 │
   │                   │───────────────────>│                    │                 │
   │                   │                   │ existsByUsername()  │                 │
   │                   │                   │───────────────────>│                 │
   │                   │                   │<─── false ──────── │                 │
   │                   │                   │ existsByEmail()     │                 │
   │                   │                   │───────────────────>│                 │
   │                   │                   │<─── false ──────── │                 │
   │                   │                   │ BCrypt.encode(pwd) │                 │
   │                   │                   │────(interno)──────>│                 │
   │                   │                   │ save(user)         │                 │
   │                   │                   │───────────────────>│  insert(user)   │
   │                   │                   │                    │────────────────>│
   │                   │                   │                    │<── user+id ──── │
   │                   │                   │ generateToken()    │                 │
   │                   │                   │────(interno)────── │                 │
   │                   │<── AuthResponse ──│                    │                 │
   │ 201 {token,user}  │                   │                    │                 │
   │<──────────────────│                   │                    │                 │
```

---

## RF02 — Autenticação de Usuário (Login)

**Descrição:** O sistema deve autenticar um usuário com username e senha, retornando um token JWT válido por 24 horas.

**Regras de Negócio:**
- Credenciais inválidas retornam 401 (mensagem genérica por segurança)
- Token expira em 24h (configurável)

**Testes Relacionados:**

| Teste | Classe | Tipo | Cenário |
|---|---|---|---|
| `shouldAuthenticateUserWithValidCredentials` | `AuthControllerIT` | Caixa Preta / E2E | Login válido retorna 200 + JWT |
| `shouldReturn401WhenPasswordIsWrong` | `AuthControllerIT` | Caixa Preta | Senha errada retorna 401 |
| `shouldReturn401WhenUserDoesNotExist` | `AuthControllerIT` | Caixa Preta | Usuário inexistente retorna 401 |
| `shouldLoginWithValidCredentials` | `UserServiceTest` | Integração (TC) | Token gerado corretamente |
| `shouldThrowExceptionWhenPasswordIsWrong` | `UserServiceTest` | Integração (TC) | Senha inválida lança exceção |
| `shouldThrowExceptionWhenUserDoesNotExist` | `UserServiceTest` | Integração (TC) | Usuário inexistente lança exceção |
| `tokenShouldContainUsernameAsSubject` | `WhiteBoxTest` | Caixa Branca | Subject do JWT = username |
| `tokenShouldContainUserIdClaim` | `WhiteBoxTest` | Caixa Branca | Claim userId presente |
| `validTokenShouldPassValidation` | `WhiteBoxTest` | Caixa Branca | Token válido aceito |
| `expiredTokenShouldFailValidation` | `WhiteBoxTest` | Caixa Branca | Token expirado rejeitado |
| `randomStringShouldBeInvalidToken` | `WhiteBoxTest` | Caixa Branca | String aleatória rejeitada |
| `tamperedTokenShouldBeInvalid` | `WhiteBoxTest` | Caixa Branca | Token adulterado rejeitado |

**Diagrama UML de Sequência — RF02:**

```
Cliente          AuthController        UserService       JwtUtil          BCryptEncoder
   │                   │                    │                │                  │
   │ POST /login        │                    │                │                  │
   │──────────────────>│                    │                │                  │
   │                   │ login(request)     │                │                  │
   │                   │───────────────────>│                │                  │
   │                   │                   │ findByUsername()│                  │
   │                   │                   │──── (MongoDB) ──>                  │
   │                   │                   │<── User ────── │                  │
   │                   │                   │                │ matches(raw,hash) │
   │                   │                   │────────────────────────────────── >│
   │                   │                   │<─────────────────────── true ───── │
   │                   │                   │ generateToken(username, userId)     │
   │                   │                   │───────────────>│                  │
   │                   │                   │<── JWT ─────── │                  │
   │                   │<── AuthResponse ──│                │                  │
   │ 200 {token,...}   │                   │                │                  │
   │<──────────────────│                   │                │                  │
```

---

## RF03 — Criação de Livro

**Descrição:** Usuário autenticado pode adicionar livros à sua biblioteca informando título, autor e demais metadados opcionais.

**Regras de Negócio:**
- Título e autor são obrigatórios
- Status padrão: `WANT_TO_READ`
- Avaliação: 1–5 (opcional)
- Livro é associado ao userId do token JWT

**Testes Relacionados:**

| Teste | Classe | Tipo | Cenário |
|---|---|---|---|
| `shouldCreateBookSuccessfully` | `BookControllerIT` | Caixa Preta / E2E | Criação retorna 201 + livro |
| `shouldReturn400WhenTitleIsMissing` | `BookControllerIT` | Caixa Preta | Título ausente retorna 400 |
| `shouldReturn401WhenNotAuthenticated` | `BookControllerIT` | Caixa Preta | Sem token retorna 403 |
| `shouldCreateBookWithDefaultStatus` | `BookServiceTest` | Integração (TC) | Status padrão WANT_TO_READ |
| `shouldCreateBookWithReadingStatus` | `BookServiceTest` | Integração (TC) | Status READING persiste |
| `shouldCreateBookWithAnyReadingStatus` | `BookServiceParameterizedTest` | Parametrizado | Todos os 3 status possíveis |
| `shouldPersistAndRetrieveVariousBooks` | `BookServiceParameterizedTest` | Parametrizado | 5 combinações título/autor/gênero |
| `shouldAcceptAllValidRatings` | `BookServiceParameterizedTest` | Parametrizado | Avaliações 1 a 5 |

**Diagrama UML de Sequência — RF03:**

```
Cliente        BookController      JwtAuthFilter     BookService      BookRepository    MongoDB
   │                │                   │                │                 │              │
   │POST /api/books │                   │                │                 │              │
   │───────────────>│                   │                │                 │              │
   │                │ validateToken()   │                │                 │              │
   │                │──────────────────>│                │                 │              │
   │                │<── userId ────── │                │                 │              │
   │                │ create(req,userId)│                │                 │              │
   │                │─────────────────────────────────> │                 │              │
   │                │                  │                │ Book.builder()  │              │
   │                │                  │                │──(interno)────> │              │
   │                │                  │                │ save(book)      │              │
   │                │                  │                │────────────────>│ insert(book) │
   │                │                  │                │                 │─────────────>│
   │                │                  │                │                 │<── book+id ──│
   │                │                  │                │<── Book ─────── │              │
   │                │<── BookResponse ─│                │                 │              │
   │ 201 BookResponse                  │                │                 │              │
   │<───────────────│                  │                │                 │              │
```

---

## RF04 — Listagem e Busca de Livros

**Descrição:** Usuário pode listar seus livros com filtros por status e busca por título.

**Testes Relacionados:**

| Teste | Classe | Tipo | Cenário |
|---|---|---|---|
| `shouldListAllBooksForUser` | `BookControllerIT` | Caixa Preta / E2E | Lista todos os livros |
| `shouldSearchBooksByTitle` | `BookControllerIT` | Caixa Preta | Busca retorna somente correspondências |
| `shouldFilterBooksByStatus` | `BookControllerIT` | Caixa Preta | Filtro por status READING |
| `shouldReturnOnlyUserBooks` | `BookServiceTest` | Integração (TC) | Isolamento por userId |
| `shouldFilterByReadStatus` | `BookServiceTest` | Integração (TC) | Filtragem por status READ |
| `shouldSearchBooksByTitleCaseInsensitive` | `BookServiceTest` | Integração (TC) | Busca case-insensitive |
| `shouldFindBooksCaseInsensitively` | `BookServiceParameterizedTest` | Parametrizado | 4 variações de capitalização |

**Diagrama UML de Sequência — RF04:**

```
Cliente        BookController      BookService          BookRepository       MongoDB
   │                │                   │                    │                 │
   │GET /api/books  │                   │                    │                 │
   │?search=harry   │                   │                    │                 │
   │───────────────>│                   │                    │                 │
   │                │ searchByTitle     │                    │                 │
   │                │   (userId,query)  │                    │                 │
   │                │──────────────────>│                    │                 │
   │                │                  │ findByUserIdAnd     │                 │
   │                │                  │ TitleContaining()   │                 │
   │                │                  │───────────────────>│                 │
   │                │                  │                    │ {regex query}   │
   │                │                  │                    │────────────────>│
   │                │                  │                    │<── [Book] ───── │
   │                │                  │<── List<Book> ──── │                 │
   │                │ map to Response  │                    │                 │
   │                │<── List<BookRes> │                    │                 │
   │ 200 [books]    │                   │                    │                 │
   │<───────────────│                   │                    │                 │
```

---

## RF05 — Atualização de Livro

**Descrição:** Usuário pode editar informações de um livro da sua biblioteca. Não pode editar livros de outros usuários.

**Testes Relacionados:**

| Teste | Classe | Tipo | Cenário |
|---|---|---|---|
| `shouldUpdateBookSuccessfully` | `BookControllerIT` | Caixa Preta / E2E | Atualização retorna 200 |
| `shouldReturn404WhenUpdatingOtherUserBook` | `BookControllerIT` | Caixa Preta | Livro de outro user retorna 404 |
| `shouldUpdateBookSuccessfully` | `BookServiceTest` | Integração (TC) | Campos atualizados + updatedAt |
| `shouldThrowExceptionWhenUpdatingOtherUserBook` | `BookServiceTest` | Integração (TC) | Exceção em livro alheio |

**Diagrama UML de Sequência — RF05:**

```
Cliente           BookController      BookService         BookRepository     MongoDB
   │                   │                   │                   │               │
   │PUT /api/books/{id}│                   │                   │               │
   │──────────────────>│                   │                   │               │
   │                   │ update(id,req,uid)│                   │               │
   │                   │──────────────────>│                   │               │
   │                   │                  │findByIdAndUserId() │               │
   │                   │                  │───────────────────>│               │
   │                   │                  │                   │{id,userId}     │
   │                   │                  │                   │───────────────>│
   │                   │                  │<── Optional<Book> ─│               │
   │                   │                  │ [se vazio: throw]  │               │
   │                   │                  │ book.set*(newData) │               │
   │                   │                  │ save(book)         │               │
   │                   │                  │───────────────────>│  update(book) │
   │                   │                  │                   │───────────────>│
   │                   │                  │                   │<── Book ────── │
   │                   │<── BookResponse ─│                   │               │
   │ 200 BookResponse  │                   │                   │               │
   │<──────────────────│                   │                   │               │
```

---

## RF06 — Remoção de Livro

**Descrição:** Usuário pode remover um livro da sua biblioteca. Não pode remover livros de outros usuários.

**Testes Relacionados:**

| Teste | Classe | Tipo | Cenário |
|---|---|---|---|
| `shouldDeleteBookSuccessfully` | `BookControllerIT` | Caixa Preta / E2E | Remoção retorna 204 |
| `shouldDeleteBookSuccessfully` | `BookServiceTest` | Integração (TC) | Livro removido do MongoDB |
| `shouldThrowExceptionWhenDeletingNonExistentBook` | `BookServiceTest` | Integração (TC) | Livro inexistente lança exceção |

**Diagrama UML de Sequência — RF06:**

```
Cliente              BookController    BookService      BookRepository    MongoDB
   │                      │                │                 │              │
   │DELETE /api/books/{id}│                │                 │              │
   │─────────────────────>│                │                 │              │
   │                      │ delete(id,uid) │                 │              │
   │                      │───────────────>│                 │              │
   │                      │               │findByIdAndUserId │              │
   │                      │               │─────────────────>│              │
   │                      │               │<── Book ──────── │              │
   │                      │               │ bookRepo.delete() │              │
   │                      │               │─────────────────>│ deleteOne()  │
   │                      │               │                 │─────────────>│
   │                      │               │                 │<── ok ──────  │
   │                      │<── void ──── │                 │              │
   │  204 No Content      │                │                 │              │
   │<─────────────────────│                │                 │              │
```

---

## RF07 — Estatísticas da Biblioteca

**Descrição:** Usuário pode consultar estatísticas da sua biblioteca: total de livros, lidos, lendo e para ler.

**Testes Relacionados:**

| Teste | Classe | Tipo | Cenário |
|---|---|---|---|
| `shouldReturnCorrectStats` | `BookControllerIT` | Caixa Preta / E2E | Stats corretos via HTTP |
| `shouldReturnCorrectStats` | `BookServiceTest` | Integração (TC) | Contadores isolados por userId |
| `shouldCalculateCompletionPercentageCorrectly` | `BookServiceParameterizedTest` | Parametrizado | 7 combinações de lidos/total |
| `completionPercentageWithZeroTotalShouldBeZero` | `WhiteBoxTest` | Caixa Branca | Ramo total=0 |
| `completionPercentageFullyShouldBe100` | `WhiteBoxTest` | Caixa Branca | Ramo 100% |
| `completionPercentageHalfShouldBe50` | `WhiteBoxTest` | Caixa Branca | Ramo 50% |
| `completionPercentageRoundingDown` | `WhiteBoxTest` | Caixa Branca | Arredondamento para baixo |
| `completionPercentageRoundingUp` | `WhiteBoxTest` | Caixa Branca | Arredondamento para cima |

---

## RF08 — Busca na Open Library (API Externa / VCR)

**Descrição:** Usuário pode buscar livros na API pública Open Library para importar dados automaticamente.

**Testes Relacionados (VCR com WireMock):**

| Teste | Classe | Tipo | Cassete (cenário gravado) |
|---|---|---|---|
| `shouldReturnBooksForValidQuery` | `OpenLibraryServiceVCRTest` | VCR | search_harry_potter |
| `shouldReturnEmptyListWhenNoBooksFound` | `OpenLibraryServiceVCRTest` | VCR | search_empty |
| `shouldThrowIOExceptionOnServerError` | `OpenLibraryServiceVCRTest` | VCR | server_error |
| `shouldHandleBookWithMissingFields` | `OpenLibraryServiceVCRTest` | VCR | partial_data |
| `shouldReturnMultipleBooksWithCompleteData` | `OpenLibraryServiceVCRTest` | VCR | search_tolkien |

**Diagrama UML de Sequência — RF08:**

```
Cliente        BookController    OpenLibraryService      WireMock/OpenLibrary API
   │                │                    │                        │
   │GET /api/books  │                    │                        │
   │/search/openlibrary?q=tolkien        │                        │
   │───────────────>│                    │                        │
   │                │ searchBooks(query) │                        │
   │                │──────────────────>│                        │
   │                │                  │ GET /search.json?q=...  │
   │                │                  │────────────────────────>│
   │                │                  │<── JSON response ─────── │
   │                │                  │ parseSearchResponse()   │
   │                │                  │──(interno)──────────── >│
   │                │<── List<OLBook> ──│                        │
   │ 200 [books]    │                   │                        │
   │<───────────────│                   │                        │
```

---

## Resumo de Cobertura por Tipo de Teste

| Tipo | Qtd. Testes | Requisitos Cobertos |
|---|---|---|
| Caixa Preta / E2E | 16 | RF01–RF08 |
| Integração (Testcontainers) | 17 | RF01–RF07 |
| Parametrizados | 5 suítes (~25 casos) | RF03, RF04, RF07 |
| VCR (WireMock) | 5 | RF08 |
| Caixa Branca | 12 | RF02, RF07 |
| **Total** | **≥ 75 casos** | **8/8 (100%)** |
