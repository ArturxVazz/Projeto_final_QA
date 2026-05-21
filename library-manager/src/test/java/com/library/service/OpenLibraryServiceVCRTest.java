package com.library.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.library.dto.BookDTOs;
import org.junit.jupiter.api.*;

import java.net.http.HttpClient;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OpenLibraryService - Testes VCR com WireMock")
class OpenLibraryServiceVCRTest {

    private static WireMockServer wireMockServer;
    private OpenLibraryService openLibraryService;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        String baseUrl = "http://localhost:" + wireMockServer.port();
        openLibraryService = new OpenLibraryService(baseUrl, HttpClient.newHttpClient());
    }

    @Test
    @DisplayName("VCR - Deve retornar livros ao buscar por query válida (cassete: search_harry_potter)")
    void shouldReturnBooksForValidQuery() throws Exception {
        // Gravação da cassete VCR (resposta da API gravada)
        wireMockServer.stubFor(get(urlPathEqualTo("/search.json"))
                .withQueryParam("q", equalTo("Harry+Potter"))
                .withQueryParam("limit", equalTo("5"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "numFound": 2,
                                "docs": [
                                    {
                                        "title": "Harry Potter and the Philosopher's Stone",
                                        "author_name": ["J.K. Rowling"],
                                        "isbn": ["9780439708180"],
                                        "first_publish_year": 1997
                                    },
                                    {
                                        "title": "Harry Potter and the Chamber of Secrets",
                                        "author_name": ["J.K. Rowling"],
                                        "isbn": ["9780439064873"],
                                        "first_publish_year": 1998
                                    }
                                ]
                            }
                            """)));

        List<BookDTOs.OpenLibraryBookDTO> results = openLibraryService.searchBooks("Harry Potter");

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getTitle()).isEqualTo("Harry Potter and the Philosopher's Stone");
        assertThat(results.get(0).getAuthor()).isEqualTo("J.K. Rowling");
        assertThat(results.get(0).getIsbn()).isEqualTo("9780439708180");
        assertThat(results.get(0).getPublishYear()).isEqualTo(1997);

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/search.json"))
                .withQueryParam("q", equalTo("Harry+Potter")));
    }

    @Test
    @DisplayName("VCR - Deve retornar lista vazia quando API não retorna docs (cassete: search_empty)")
    void shouldReturnEmptyListWhenNoBooksFound() throws Exception {
        wireMockServer.stubFor(get(urlPathEqualTo("/search.json"))
                .withQueryParam("q", equalTo("xyzqwerty123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "numFound": 0,
                                "docs": []
                            }
                            """)));

        List<BookDTOs.OpenLibraryBookDTO> results = openLibraryService.searchBooks("xyzqwerty123");

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("VCR - Deve lançar IOException quando API retorna erro 500 (cassete: server_error)")
    void shouldThrowIOExceptionOnServerError() {
        wireMockServer.stubFor(get(urlPathEqualTo("/search.json"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        assertThatThrownBy(() -> openLibraryService.searchBooks("qualquer coisa"))
                .isInstanceOf(java.io.IOException.class)
                .hasMessageContaining("500");
    }

    @Test
    @DisplayName("VCR - Deve tratar livro sem autor e sem ISBN corretamente (cassete: partial_data)")
    void shouldHandleBookWithMissingFields() throws Exception {
        wireMockServer.stubFor(get(urlPathEqualTo("/search.json"))
                .withQueryParam("q", equalTo("Livro+sem+dados"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "numFound": 1,
                                "docs": [
                                    {
                                        "title": "Livro Sem Autor"
                                    }
                                ]
                            }
                            """)));

        List<BookDTOs.OpenLibraryBookDTO> results = openLibraryService.searchBooks("Livro sem dados");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Livro Sem Autor");
        assertThat(results.get(0).getAuthor()).isNull();
        assertThat(results.get(0).getIsbn()).isNull();
    }

    @Test
    @DisplayName("VCR - Deve retornar múltiplos livros com dados completos (cassete: search_tolkien)")
    void shouldReturnMultipleBooksWithCompleteData() throws Exception {
        wireMockServer.stubFor(get(urlPathEqualTo("/search.json"))
                .withQueryParam("q", equalTo("Tolkien"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "numFound": 3,
                                "docs": [
                                    {
                                        "title": "The Hobbit",
                                        "author_name": ["J.R.R. Tolkien"],
                                        "isbn": ["9780547928227"],
                                        "first_publish_year": 1937
                                    },
                                    {
                                        "title": "The Fellowship of the Ring",
                                        "author_name": ["J.R.R. Tolkien"],
                                        "isbn": ["9780618346257"],
                                        "first_publish_year": 1954
                                    },
                                    {
                                        "title": "The Two Towers",
                                        "author_name": ["J.R.R. Tolkien"],
                                        "isbn": ["9780618346264"],
                                        "first_publish_year": 1954
                                    }
                                ]
                            }
                            """)));

        List<BookDTOs.OpenLibraryBookDTO> results = openLibraryService.searchBooks("Tolkien");

        assertThat(results).hasSize(3);
        assertThat(results).extracting(BookDTOs.OpenLibraryBookDTO::getAuthor)
                .containsOnly("J.R.R. Tolkien");
    }
}
