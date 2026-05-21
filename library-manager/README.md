# 📚 Biblioteca Pessoal — Gerenciador de Leitura

Sistema completo de gerenciamento de biblioteca pessoal com autenticação JWT, CRUD de livros e integração com a API Open Library.

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Backend | Spring Boot 3.2, Java 17 |
| Banco de Dados | MongoDB 7.0 |
| Autenticação | JWT (jjwt 0.12) |
| Testes | JUnit 5, Testcontainers, WireMock (VCR) |
| Cobertura | JaCoCo ≥ 80% |
| Qualidade | SonarQube / SonarCloud |
| CI/CD | GitHub Actions |
| Frontend | HTML5, CSS3, JavaScript (SPA) |

---

## 🏗️ Arquitetura MVC

```
src/main/java/com/library/
├── config/          → SecurityConfig, JwtUtil, JwtAuthFilter
├── model/           → User, Book (documentos MongoDB)
├── repository/      → UserRepository, BookRepository
├── service/         → UserService, BookService, OpenLibraryService
├── controller/      → AuthController, BookController
└── dto/             → AuthDTOs, BookDTOs
```

---

## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- Docker e Docker Compose (para MongoDB)
- Maven 3.8+

### 1. Subir o MongoDB com Docker
```bash
docker run -d --name mongodb -p 27017:27017 mongo:7.0
```

### 2. Executar a aplicação
```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em: **http://localhost:8080**

### 3. Executar com variáveis de ambiente personalizadas
```bash
MONGODB_URI=mongodb://localhost:27017/librarydb \
JWT_SECRET=sua-chave-secreta-aqui \
./mvnw spring-boot:run
```

---

## 🧪 Executar os Testes

```bash
# Todos os testes + relatório de cobertura JaCoCo
./mvnw clean verify

# Relatório HTML disponível em:
# target/site/jacoco/index.html
```

> ⚠️ Os testes utilizam **Testcontainers** — o Docker deve estar rodando na máquina.

---

## 📊 Relatório de Cobertura

Após executar `mvn verify`, abra:
```
target/site/jacoco/index.html
```

A configuração exige **mínimo de 80% de cobertura de linhas** (JaCoCo check). O build falha automaticamente se não atingido.

---

## 🔌 API REST

### Autenticação

| Método | Endpoint | Descrição | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Cadastrar usuário | ❌ |
| POST | `/api/auth/login` | Autenticar usuário | ❌ |

**Exemplo de registro:**
```json
POST /api/auth/register
{
  "username": "joao_silva",
  "email": "joao@email.com",
  "password": "senha123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "joao_silva",
  "userId": "65abc123..."
}
```

### Livros (requer `Authorization: Bearer <token>`)

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/books` | Listar todos os livros |
| GET | `/api/books?status=READ` | Filtrar por status |
| GET | `/api/books?search=harry` | Buscar por título |
| POST | `/api/books` | Criar livro |
| GET | `/api/books/{id}` | Buscar por ID |
| PUT | `/api/books/{id}` | Atualizar livro |
| DELETE | `/api/books/{id}` | Remover livro |
| GET | `/api/books/stats` | Estatísticas do usuário |
| GET | `/api/books/search/openlibrary?q=tolkien` | Busca na Open Library |

**Status disponíveis:** `WANT_TO_READ`, `READING`, `READ`

---

## 🔍 Integração SonarQube

### SonarCloud (CI)
Configure os secrets no GitHub:
- `SONAR_TOKEN` — token gerado no SonarCloud
- `SONAR_ORGANIZATION` (variable) — nome da organização

### Execução local
```bash
./mvnw sonar:sonar \
  -Dsonar.projectKey=library-manager \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=SEU_TOKEN
```

---

## 🧩 Estratégia de Testes

| Tipo | Classe | Descrição |
|---|---|---|
| Integração E2E (Caixa Preta) | `AuthControllerIT` | Fluxos completos de auth via HTTP |
| Integração E2E (Caixa Preta) | `BookControllerIT` | CRUD completo via MockMvc |
| Integração (Testcontainers) | `BookServiceTest` | Serviço com MongoDB real |
| Integração (Testcontainers) | `UserServiceTest` | Serviço com MongoDB real |
| Parametrizados | `BookServiceParameterizedTest` | Múltiplos cenários por parâmetro |
| VCR (WireMock) | `OpenLibraryServiceVCRTest` | API externa simulada |
| Caixa Branca | `WhiteBoxTest` | Lógica interna de JWT e cálculos |

> ✅ **Nenhum mock** (Mockito) é utilizado — todos os testes usam Testcontainers ou WireMock.

---

## 📁 Variáveis de Ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/librarydb` | URI do MongoDB |
| `JWT_SECRET` | (valor interno) | Chave secreta JWT (mínimo 256 bits) |
| `JWT_EXPIRATION` | `86400000` | Expiração do token em ms (24h) |
