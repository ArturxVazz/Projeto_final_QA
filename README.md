# 📚 Biblioteca Pessoal

Sistema completo de gerenciamento de biblioteca pessoal com autenticação de usuários, CRUD de livros, testes automatizados (Testcontainers + WireMock) e integração com SonarQube + GitHub Actions.

---

## Sumário

- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Requisitos](#requisitos)
- [Execução Local](#execução-local)
- [Testes](#testes)
- [Cobertura de Código (JaCoCo)](#cobertura-de-código-jacoco)
- [Qualidade com SonarQube](#qualidade-com-sonarqube)
- [CI/CD com GitHub Actions](#cicd-com-github-actions)
- [Documentação da API](#documentação-da-api)
- [Estrutura do Projeto](#estrutura-do-projeto)

---

## Tecnologias

### Backend
| Tecnologia          | Versão  | Uso                                      |
|---------------------|---------|------------------------------------------|
| Java                | 17      | Linguagem principal                      |
| Spring Boot         | 3.2.3   | Framework web                            |
| Spring Security     | 6.x     | Autenticação e autorização               |
| MongoDB             | 7.0     | Banco de dados NoSQL                     |
| Spring Data MongoDB | —       | Persistência (arquitetura MVC)           |
| JWT (jjwt)          | 0.12.5  | Tokens de autenticação stateless         |
| Testcontainers      | 1.19.6  | Testes de integração com MongoDB real    |
| WireMock            | 3.4.2   | VCR para APIs externas                   |
| JaCoCo              | 0.8.11  | Cobertura de código (mín. 80%)           |
| SonarQube           | 10.x    | Análise estática de qualidade            |

### Frontend
| Tecnologia    | Versão | Uso                          |
|---------------|--------|------------------------------|
| React         | 18.2   | UI framework                 |
| TypeScript    | 5.2    | Tipagem estática             |
| Vite          | 5.1    | Build tool                   |
| React Router  | 6.22   | Roteamento SPA               |
| Axios         | 1.6    | HTTP client                  |

---

## Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                        FRONTEND (React)                      │
│   LoginPage  RegisterPage  BooksPage  BookCard  BookModal    │
│              AuthContext (JWT gerenciamento de sessão)       │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTP + JWT Bearer Token
┌───────────────────────────▼─────────────────────────────────┐
│                    BACKEND (Spring Boot)                      │
│                                                              │
│  AuthController  BookController  UserController              │
│       │                │                │                    │
│  AuthService      BookService      UserService               │
│       │                │                │                    │
│  JwtService    BookRepository   UserRepository               │
│                        │                │                    │
│              Spring Data MongoDB (MVC)                       │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                      MongoDB (NoSQL)                          │
│              Collections: users, books                       │
└─────────────────────────────────────────────────────────────┘
```

**Padrões aplicados:** MVC, Repository, Service Layer, JWT Stateless Auth, DTO Pattern.

---

## Requisitos

- Java 17+
- Maven 3.8+
- Docker e Docker Compose
- Node.js 20+ (frontend)

---

## Execução Local

### Com Docker Compose (recomendado)

```bash
# Subir todos os serviços: MongoDB, SonarQube, Backend, Frontend
docker-compose up -d

# Acessar:
# Frontend:  http://localhost:3000
# Backend:   http://localhost:8080
# Swagger:   http://localhost:8080/swagger-ui.html
# SonarQube: http://localhost:9000 (admin/admin)
```

### Backend (sem Docker)

```bash
cd backend

# Certifique-se que o MongoDB está rodando localmente
# Configurar variáveis de ambiente (opcional):
export MONGODB_URI=mongodb://localhost:27017/biblioteca
export JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

mvn spring-boot:run
```

### Frontend (sem Docker)

```bash
cd frontend
npm install
npm run dev
# Acesse: http://localhost:3000
```

---

## Testes

### Estratégia de Testes

> ⚠️ **O uso de Mocks está proibido no projeto.** Utilize **Testcontainers** para persistência e **WireMock (VCR)** para chamadas a APIs externas.

| Tipo                     | Ferramenta              | Localização                        |
|--------------------------|-------------------------|------------------------------------|
| Unitários (Caixa Branca) | JUnit 5 + Mockito       | `service/BookServiceTest`          |
| Integração               | Testcontainers + MongoDB| `integration/BookIntegrationTest`  |
| Caixa Preta (Controller) | MockMvc                 | `controller/BookControllerTest`    |
| Parametrizados           | JUnit 5 @ParameterizedTest | `service/BookServiceTest`       |
| Repository (MongoDB)     | Testcontainers          | `repository/BookRepositoryTest`    |
| VCR (APIs externas)      | WireMock                | `integration/ExternalApiVcrTest`   |

### Executar testes

```bash
cd backend

# Todos os testes
mvn test

# Apenas unitários (sem Testcontainers)
mvn test -pl . -Dtest="*ServiceTest,*ControllerTest"

# Apenas integração
mvn test -Dtest="*IntegrationTest,*RepositoryTest"

# Com relatório de cobertura
mvn test jacoco:report
# Relatório em: target/site/jacoco/index.html
```

---

## Cobertura de Código (JaCoCo)

O pipeline falha automaticamente se a cobertura de linhas ficar abaixo de **80%**.

```bash
cd backend

# Verificar cobertura
mvn jacoco:check

# Gerar relatório HTML
mvn jacoco:report
open target/site/jacoco/index.html
```

Exclusões configuradas (não contam para a cobertura):
- `com/biblioteca/dto/**` — DTOs (dados puros)
- `com/biblioteca/model/**` — Entidades
- `BibliotecaApplication.class` — Entry point

---

## Qualidade com SonarQube

```bash
# Subir SonarQube (via Docker Compose)
docker-compose up sonarqube -d

# Aguardar inicialização (~1 min) e acessar http://localhost:9000
# Login: admin / admin → criar token em My Account > Security

# Executar análise
cd backend
mvn sonar:sonar \
  -Dsonar.projectKey=biblioteca-pessoal \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=SEU_TOKEN_AQUI
```

Configuração completa em `backend/sonar-project.properties`.

---

## CI/CD com GitHub Actions

O pipeline em `.github/workflows/ci.yml` executa automaticamente em todo push e pull request para `main` e `develop`:

```
Push/PR
   │
   ├─► Job 1: backend-ci
   │     ├─ Compile (mvn compile)
   │     ├─ Test (mvn test) — Testcontainers + WireMock
   │     ├─ Coverage Check (jacoco:check ≥ 80%)
   │     ├─ JaCoCo Report (upload artifact)
   │     ├─ SonarQube Analysis
   │     └─ Build JAR
   │
   ├─► Job 2: frontend-ci
   │     ├─ Install (npm ci)
   │     ├─ Lint (npm run lint)
   │     └─ Build (npm run build)
   │
   ├─► Job 3: quality-gate
   │     └─ Verifica resultado dos jobs anteriores
   │
   └─► Job 4: deploy-staging (apenas branch main)
         └─ Deploy do JAR para staging
```

### Configurar Secrets no GitHub

Acesse **Settings > Secrets and variables > Actions** e adicione:

| Secret           | Valor                                    |
|------------------|------------------------------------------|
| `SONAR_TOKEN`    | Token gerado no SonarQube                |
| `SONAR_HOST_URL` | URL do servidor SonarQube                |

---

## Documentação da API

A documentação interativa (Swagger UI) está disponível em:

```
http://localhost:8080/swagger-ui.html
```

### Endpoints principais

| Método | Endpoint                  | Descrição                    | Auth |
|--------|---------------------------|------------------------------|------|
| POST   | `/api/auth/register`      | Cadastrar usuário            | ❌   |
| POST   | `/api/auth/login`         | Login + JWT                  | ❌   |
| GET    | `/api/users/me`           | Dados do usuário autenticado | ✅   |
| GET    | `/api/books`              | Listar livros                | ✅   |
| POST   | `/api/books`              | Criar livro                  | ✅   |
| GET    | `/api/books/{id}`         | Buscar livro                 | ✅   |
| PUT    | `/api/books/{id}`         | Atualizar livro              | ✅   |
| DELETE | `/api/books/{id}`         | Excluir livro                | ✅   |
| GET    | `/api/books/status/{s}`   | Filtrar por status           | ✅   |
| GET    | `/api/books/search?title=`| Buscar por título            | ✅   |

---

## Estrutura do Projeto

```
biblioteca-pessoal/
├── .github/
│   └── workflows/
│       └── ci.yml                  # Pipeline CI/CD completo
├── backend/
│   ├── src/
│   │   ├── main/java/com/biblioteca/
│   │   │   ├── config/             # SecurityConfig, OpenApiConfig
│   │   │   ├── controller/         # AuthController, BookController, UserController
│   │   │   ├── dto/                # Request/Response DTOs
│   │   │   ├── exception/          # GlobalExceptionHandler, custom exceptions
│   │   │   ├── model/              # User, Book (entidades MongoDB)
│   │   │   ├── repository/         # UserRepository, BookRepository
│   │   │   ├── security/           # JwtService, JwtAuthFilter, UserDetailsService
│   │   │   └── service/            # AuthService, BookService, UserService
│   │   └── test/java/com/biblioteca/
│   │       ├── controller/         # BookControllerTest (Caixa Preta)
│   │       ├── integration/        # BaseIntegrationTest, Auth/BookIntegrationTest, VCR
│   │       ├── repository/         # BookRepositoryTest (Testcontainers)
│   │       └── service/            # BookServiceTest, UserServiceTest (Caixa Branca)
│   ├── Dockerfile
│   ├── pom.xml                     # Deps + JaCoCo (80%) + Sonar
│   └── sonar-project.properties
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── auth/               # LoginPage, RegisterPage
│   │   │   ├── books/              # BooksPage, BookCard, BookModal
│   │   │   └── layout/             # Layout, Header, Footer
│   │   ├── hooks/                  # useAuth (AuthContext + session management)
│   │   ├── services/               # api.ts, authService.ts, bookService.ts
│   │   └── styles/                 # global.css
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
├── docs/
│   └── RTM.md                      # Matriz de Rastreabilidade + UML de Sequência
├── docker-compose.yml              # MongoDB + SonarQube + Backend + Frontend
└── README.md
```

---

## Checklist de Requisitos

- [x] **Spring Boot (Java)** — Backend completo com MVC
- [x] **MongoDB (NoSQL)** — Persistência com Spring Data MongoDB
- [x] **Arquitetura MVC** — Controllers, Services, Repositories
- [x] **Testcontainers & VCR** — Testes de integração reais (sem mocks)
- [x] **Integração com SonarQube** — `sonar-project.properties` + Maven plugin
- [x] **CI completo com GitHub Actions** — Build, Test, Coverage, Sonar, Deploy
- [x] **Cadastro de Usuários** — Registro + login com JWT
- [x] **Interface Web Funcional** — CRUD completo de livros
- [x] **Gerenciamento de Sessão** — JWT stateless + AuthContext no frontend
- [x] **Design Responsivo** — CSS responsivo com media queries
- [x] **Experiência do Usuário (UX)** — Feedback visual, loading states, validações
- [x] **RTM.md** — Matriz de rastreabilidade com UML de sequência
- [x] **Cobertura ≥ 80%** — JaCoCo configurado com threshold e relatório
- [x] **Mínimo 80% Cobertura** — Verificação automática no pipeline
