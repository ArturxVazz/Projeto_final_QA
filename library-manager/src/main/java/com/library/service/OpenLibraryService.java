package com.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.BookDTOs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpenLibraryService {

    @Value("${openlibrary.base-url:https://openlibrary.org}")
    private String baseUrl;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenLibraryService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public OpenLibraryService(String baseUrl, HttpClient httpClient) {
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    public List<BookDTOs.OpenLibraryBookDTO> searchBooks(String query) throws IOException, InterruptedException {
        String encodedQuery = query.replace(" ", "+");
        String url = baseUrl + "/search.json?q=" + encodedQuery + "&limit=5&fields=title,author_name,isbn,first_publish_year";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("OpenLibrary API retornou status: " + response.statusCode());
        }

        return parseSearchResponse(response.body());
    }

    private List<BookDTOs.OpenLibraryBookDTO> parseSearchResponse(String body) throws IOException {
        List<BookDTOs.OpenLibraryBookDTO> results = new ArrayList<>();
        JsonNode root = objectMapper.readTree(body);
        JsonNode docs = root.get("docs");

        if (docs == null || !docs.isArray()) return results;

        for (JsonNode doc : docs) {
            BookDTOs.OpenLibraryBookDTO dto = new BookDTOs.OpenLibraryBookDTO();
            dto.setTitle(getTextOrNull(doc, "title"));

            JsonNode authors = doc.get("author_name");
            if (authors != null && authors.isArray() && authors.size() > 0) {
                dto.setAuthor(authors.get(0).asText());
            }

            JsonNode isbns = doc.get("isbn");
            if (isbns != null && isbns.isArray() && isbns.size() > 0) {
                dto.setIsbn(isbns.get(0).asText());
            }

            JsonNode year = doc.get("first_publish_year");
            if (year != null && !year.isNull()) {
                dto.setPublishYear(year.asInt());
            }

            results.add(dto);
        }

        return results;
    }

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value != null && !value.isNull()) ? value.asText() : null;
    }
}
