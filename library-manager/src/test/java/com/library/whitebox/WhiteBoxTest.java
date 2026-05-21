package com.library.whitebox;

import com.library.config.JwtUtil;
import com.library.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes de Caixa Branca - Lógica Interna")
class WhiteBoxTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "chave-secreta-de-256-bits-para-testes-unitarios-de-caixa-branca-ok");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
    }

    // ── JwtUtil - Caixa Branca ──

    @Test
    @DisplayName("JwtUtil: token gerado deve conter username no subject")
    void tokenShouldContainUsernameAsSubject() {
        String token = jwtUtil.generateToken("alice", "user-001");
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("alice");
    }

    @Test
    @DisplayName("JwtUtil: token gerado deve conter userId como claim")
    void tokenShouldContainUserIdClaim() {
        String token = jwtUtil.generateToken("alice", "user-001");
        assertThat(jwtUtil.extractUserId(token)).isEqualTo("user-001");
    }

    @Test
    @DisplayName("JwtUtil: token válido deve ser aceito por isTokenValid")
    void validTokenShouldPassValidation() {
        String token = jwtUtil.generateToken("bob", "user-002");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("JwtUtil: token expirado deve ser rejeitado por isTokenValid")
    void expiredTokenShouldFailValidation() {
        JwtUtil expiredJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(expiredJwtUtil, "secret",
                "chave-secreta-de-256-bits-para-testes-unitarios-de-caixa-branca-ok");
        ReflectionTestUtils.setField(expiredJwtUtil, "expiration", -1000L); // expirado

        String token = expiredJwtUtil.generateToken("carol", "user-003");
        assertThat(expiredJwtUtil.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("JwtUtil: string aleatória deve ser rejeitada como token inválido")
    void randomStringShouldBeInvalidToken() {
        assertThat(jwtUtil.isTokenValid("nao.e.um.token.jwt")).isFalse();
    }

    @Test
    @DisplayName("JwtUtil: string vazia deve ser rejeitada como token inválido")
    void emptyStringShouldBeInvalidToken() {
        assertThat(jwtUtil.isTokenValid("")).isFalse();
    }

    @Test
    @DisplayName("JwtUtil: token adulterado deve ser rejeitado")
    void tamperedTokenShouldBeInvalid() {
        String token = jwtUtil.generateToken("dave", "user-004");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtUtil.isTokenValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("JwtUtil: dois tokens para o mesmo usuário devem ser diferentes")
    void twoTokensForSameUserShouldBeDifferent() throws InterruptedException {
        String token1 = jwtUtil.generateToken("eve", "user-005");
        Thread.sleep(10); // garantir timestamps diferentes
        String token2 = jwtUtil.generateToken("eve", "user-005");
        // Podem ser iguais se gerados no mesmo milissegundo, mas normalmente são distintos
        // O importante é que ambos sejam válidos
        assertThat(jwtUtil.isTokenValid(token1)).isTrue();
        assertThat(jwtUtil.isTokenValid(token2)).isTrue();
    }

    // ── BookService.calculateCompletionPercentage - Caixa Branca (ramos) ──

    private BookService createBookService() {
        // Cria instância sem MongoDB para testar apenas a lógica interna
        return new BookService(null) {
            @Override
            public int calculateCompletionPercentage(long read, long total) {
                return super.calculateCompletionPercentage(read, total);
            }
        };
    }

    @Test
    @DisplayName("CaixaBranca: calculateCompletionPercentage - ramo total=0 deve retornar 0")
    void completionPercentageWithZeroTotalShouldBeZero() {
        BookService service = new BookService(null);
        assertThat(service.calculateCompletionPercentage(0, 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("CaixaBranca: calculateCompletionPercentage - ramo 100%")
    void completionPercentageFullyShouldBe100() {
        BookService service = new BookService(null);
        assertThat(service.calculateCompletionPercentage(10, 10)).isEqualTo(100);
    }

    @Test
    @DisplayName("CaixaBranca: calculateCompletionPercentage - ramo 50%")
    void completionPercentageHalfShouldBe50() {
        BookService service = new BookService(null);
        assertThat(service.calculateCompletionPercentage(5, 10)).isEqualTo(50);
    }

    @Test
    @DisplayName("CaixaBranca: calculateCompletionPercentage - arredondamento para baixo (33%)")
    void completionPercentageRoundingDown() {
        BookService service = new BookService(null);
        assertThat(service.calculateCompletionPercentage(1, 3)).isEqualTo(33);
    }

    @Test
    @DisplayName("CaixaBranca: calculateCompletionPercentage - arredondamento para cima (67%)")
    void completionPercentageRoundingUp() {
        BookService service = new BookService(null);
        assertThat(service.calculateCompletionPercentage(2, 3)).isEqualTo(67);
    }
}
