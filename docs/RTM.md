# RTM — Matriz de Rastreabilidade de Requisitos

> **Biblioteca Pessoal** · v1.0.0  
> Cobertura alvo: **≥ 80%** (verificada via JaCoCo)  
> Mocks: **proibidos** — persistência via **Testcontainers**, APIs externas via **WireMock (VCR)**

---

## Índice
- [Requisitos Funcionais](#requisitos-funcionais)
- [Mapeamento Requisito → Teste](#mapeamento-requisito--teste)
- [Diagramas UML de Sequência](#diagramas-uml-de-sequência)

---

## Requisitos Funcionais

| ID     | Requisito                                                                   | Módulo   | Prioridade |
|--------|-----------------------------------------------------------------------------|----------|------------|
| RF-001 | Cadastrar novo usuário (username, email, senha)                              | Auth     | Alta       |
| RF-002 | Realizar login com JWT (gerenciamento de sessão)                             | Auth     | Alta       |
| RF-003 | Criar livro (título, autor, ISBN, gênero, ano, status, avaliação)            | Books    | Alta       |
| RF-004 | Listar todos os livros do usuário autenticado                                | Books    | Alta       |
| RF-005 | Atualizar dados de um livro existente                                        | Books    | Alta       |
| RF-006 | Excluir livro                                                                | Books    | Alta       |
| RF-007 | Filtrar livros por status de leitura                                         | Books    | Média      |
| RF-008 | Buscar livros por título (case-insensitive)                                  | Books    | Média      |
| RF-009 | Validar campos obrigatórios e formatos                                       | Cross    | Alta       |
| RF-010 | Impedir acesso a livros de outro usuário                                     | Security | Alta       |

---

## Mapeamento Requisito → Teste

| Req.    | Classe de Teste           | Método de Teste                          | Tipo                      | Ferramenta       |
|---------|---------------------------|------------------------------------------|---------------------------|------------------|
| RF-001  | AuthIntegrationTest       | shouldRegisterUserSuccessfully           | Integração                | Testcontainers   |
| RF-001  | AuthIntegrationTest       | shouldRejectDuplicateUsername            | Integração                | Testcontainers   |
| RF-001  | UserServiceTest           | shouldRegisterUserWithEncodedPassword    | Unitário (Caixa Branca)   | Mockito          |
| RF-001  | UserServiceTest           | shouldThrowForDuplicateUsername          | Unitário (Caixa Branca)   | Mockito          |
| RF-002  | AuthIntegrationTest       | shouldLoginAndReturnJwt                  | Integração                | Testcontainers   |
| RF-002  | AuthIntegrationTest       | shouldRejectInvalidCredentials           | Integração                | Testcontainers   |
| RF-003  | BookIntegrationTest       | shouldCreateBook                         | Integração                | Testcontainers   |
| RF-003  | BookServiceTest           | shouldCreateBook                         | Unitário (Caixa Branca)   | Mockito          |
| RF-003  | BookServiceTest           | shouldThrowOnDuplicateIsbn               | Unitário (Caixa Branca)   | Mockito          |
| RF-003  | BookControllerTest        | shouldReturn201OnCreate                  | Caixa Preta (Controller)  | MockMvc          |
| RF-004  | BookIntegrationTest       | shouldListUserBooks                      | Integração                | Testcontainers   |
| RF-004  | BookServiceTest           | shouldFindAllByUser                      | Unitário (Caixa Branca)   | Mockito          |
| RF-004  | BookControllerTest        | shouldReturn200WithBooks                 | Caixa Preta (Controller)  | MockMvc          |
| RF-005  | BookIntegrationTest       | shouldUpdateBook                         | Integração                | Testcontainers   |
| RF-005  | BookServiceTest           | shouldAcceptAllValidRatings              | Parametrizado             | JUnit 5 Params   |
| RF-006  | BookIntegrationTest       | shouldDeleteBook                         | Integração                | Testcontainers   |
| RF-006  | BookServiceTest           | shouldDeleteBook                         | Unitário (Caixa Branca)   | Mockito          |
| RF-007  | BookServiceTest           | shouldFindByAllStatuses                  | Parametrizado (@EnumSource)| JUnit 5 Params  |
| RF-007  | BookRepositoryTest        | shouldFilterByStatus                     | Integração (MongoDB)      | Testcontainers   |
| RF-008  | BookRepositoryTest        | shouldSearchTitleCaseInsensitive         | Integração (MongoDB)      | Testcontainers   |
| RF-009  | BookControllerTest        | shouldReturn400WhenTitleIsBlank          | Param. / Caixa Preta      | MockMvc          |
| RF-010  | BookServiceTest           | shouldThrowWhenNotOwner                  | Unitário (Caixa Branca)   | Mockito          |
| RF-010  | BookControllerTest        | shouldReturn401WithoutAuth               | Caixa Preta (Controller)  | MockMvc          |
| VCR-001 | ExternalApiVcrTest        | shouldReturnBookDataFromExternalApi      | VCR                       | WireMock         |
| VCR-002 | ExternalApiVcrTest        | shouldHandleExternalApiFailure           | VCR                       | WireMock         |
| VCR-003 | ExternalApiVcrTest        | shouldHandleExternalApiTimeout           | VCR                       | WireMock         |

---

## Diagramas UML de Sequência

### RF-001 + RF-002 — Cadastro e Login

```
Client        AuthController    AuthService      UserService     UserRepository   PasswordEncoder
  |                 |                |                |                |                |
  |--POST /register->               |                |                |                |
  |                 |--register()--->                |                |                |
  |                 |               |--register()---->                |                |
  |                 |               |                |--existsByUsername()-->          |
  |                 |               |                |<--false---------                |
  |                 |               |                |--encode(password)-------------->|
  |                 |               |                |<--hashed-----------------------|
  |                 |               |                |--save(user)---->               |
  |                 |               |                |<--User{id}------               |
  |                 |               |<--UserResponse-|                |               |
  |<--201 Created---|               |                |                |               |
  |                 |               |                |                |               |
  |--POST /login--->|               |                |                |               |
  |                 |--login()------>               |                |               |
  |                 |               |--authenticate(AuthManager)      |               |
  |                 |               |--generateToken(JwtService)      |               |
  |                 |               |--findByUsername()-->            |               |
  |                 |               |<--UserResponse--                |               |
  |<--200+JWT token-|               |                |               |               |
```

### RF-003 — Criar Livro

```
Client(JWT)   BookController    BookService     BookRepository
  |                 |                |                |
  |--POST /books--->                 |                |
  |                 |--getUserId()   |                |
  |                 |--create(req,userId)-->          |
  |                 |               |--existsByIsbn()->               |
  |                 |               |<--false---------                |
  |                 |               |--save(Book)---->               |
  |                 |               |<--Book{id}------               |
  |<--201 Created---|               |                |               |
  |                 |               |                |               |
  | [ISBN duplicado]|               |                |               |
  |--POST /books--->                |                |               |
  |                 |--create()---->                 |               |
  |                 |               |--existsByIsbn()->               |
  |                 |               |<--true----------                |
  |                 |               |--throw DuplicateResourceException
  |<--409 Conflict--|               |                |               |
```

### RF-005 — Atualizar Livro

```
Client(JWT)   BookController    BookService     BookRepository
  |                 |                |                |
  |--PUT /books/{id}->               |                |
  |                 |--update(id,req,userId)-->       |
  |                 |               |--findById(id)-->|
  |                 |               |<--Book----------|
  |                 |               |--validateOwnership()
  |                 |               |--set fields + updatedAt
  |                 |               |--save(book)---->|
  |                 |               |<--Book(updated)-|
  |<--200 OK--------|               |                |
```

### RF-010 — Controle de Acesso

```
Client(user-A)   BookService     BookRepository
  |                   |                |
  |--GET livro do user-B-->            |
  |                   |--findById()---->
  |                   |<--Book{userId:"user-B"}
  |                   |--validateOwnership("user-A" != "user-B")
  |                   |--throw UnauthorizedException
  |<--403 Forbidden---|                |
```

### VCR — WireMock para APIs Externas

```
Test        WireMockServer(8089)    Open Library (não chamado)
  |                |                          |
  |--stubFor(GET /isbn/{isbn})-->             |
  |                |--registra stub           |
  |                |                          |
  |--GET localhost:8089/isbn/...-->           |
  |                |--replay stub             |
  |<--200 + JSON---|                          |
  |--verify assertions-->                     |
```

---

*Cobertura verificada por JaCoCo — threshold mínimo 80% configurado no `pom.xml`.*
