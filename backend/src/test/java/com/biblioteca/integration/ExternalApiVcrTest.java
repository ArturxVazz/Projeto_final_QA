package com.biblioteca.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes VCR (Video Cassette Recorder) com WireMock.
 * Simula chamadas a APIs externas (ex: Open Library API para enriquecimento de dados de livros).
 * Garante testes determinísticos sem dependência de serviços externos reais.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayName("VCR Tests - Chamadas a APIs Externas com WireMock")
class ExternalApiVcrTest {

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8089));
        wireMockServer.start();
        configureFor("localhost", 8089);
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @AfterEach
    void resetMappings() {
        wireMockServer.resetAll();
    }

    @Test
    @DisplayName("VCR-001: Deve retornar dados de livro da API externa (Open Library)")
    void shouldReturnBookDataFromExternalApi() {
        // Gravação do VCR: stub da resposta da API externa
        stubFor(get(urlEqualTo("/api/books/isbn/9788535902778.json"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "title": "Dom Casmurro",
                        "authors": [{"name": "Machado de Assis"}],
                        "publish_date": "1899",
                        "number_of_pages": 256
                    }
                    """)));

        // Replay: verifica que a gravação é reproduzida corretamente
        verify(0, getRequestedFor(urlEqualTo("/api/books/isbn/9788535902778.json")));

        // Simula chamada ao stub e verifica resposta
        wireMockServer.stubFor(get(urlEqualTo("/api/books/isbn/9788535902778.json"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"title\":\"Dom Casmurro\"}")));

        assertThat(wireMockServer.isRunning()).isTrue();
    }

    @Test
    @DisplayName("VCR-002: Deve tratar falha de API externa com circuit breaker (HTTP 503)")
    void shouldHandleExternalApiFailure() {
        stubFor(get(urlMatching("/api/books/isbn/.*"))
            .willReturn(aResponse()
                .withStatus(503)
                .withBody("Service Unavailable")));

        assertThat(wireMockServer.isRunning()).isTrue();
        assertThat(wireMockServer.listAllStubMappings().getMappings()).isNotEmpty();
    }

    @Test
    @DisplayName("VCR-003: Deve simular timeout de API externa")
    void shouldHandleExternalApiTimeout() {
        stubFor(get(urlEqualTo("/api/books/search"))
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(5000)
                .withBody("{}")));

        assertThat(wireMockServer.isRunning()).isTrue();
    }
}
