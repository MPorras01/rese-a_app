package com.resenias.reviews.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Geocodificación usando Nominatim (OpenStreetMap) — 100% gratuito, sin API key.
 * Límite de uso: 1 request/segundo (política de Nominatim).
 * Docs: https://nominatim.org/release-docs/develop/api/Search/
 */
@Service
public class GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT = "ReseniaApp/1.0 (contact@reseniaapp.com)";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeocodingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    /**
     * Geocodifica una dirección y retorna [lat, lng] o null si no se encontró.
     */
    public double[] geocode(String address, String city) {
        String query = buildQuery(address, city);
        if (query.isBlank()) return null;

        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = NOMINATIM_URL + "?q=" + encoded + "&format=json&limit=1";

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Accept-Language", "es")
                .GET()
                .timeout(Duration.ofSeconds(8))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Nominatim respondió {}: {}", response.statusCode(), query);
                return null;
            }

            List<NominatimResult> results = objectMapper.readValue(
                response.body(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, NominatimResult.class)
            );

            if (results.isEmpty()) {
                log.debug("Sin resultados de geocodificación para: {}", query);
                return null;
            }

            NominatimResult result = results.get(0);
            double lat = Double.parseDouble(result.lat());
            double lon = Double.parseDouble(result.lon());
            log.debug("Geocodificado '{}' → [{}, {}]", query, lat, lon);
            return new double[]{lat, lon};

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Geocodificación interrumpida para: {}", query);
            return null;
        } catch (Exception e) {
            log.warn("Error geocodificando '{}': {}", query, e.getMessage());
            return null;
        }
    }

    private String buildQuery(String address, String city) {
        StringBuilder sb = new StringBuilder();
        if (address != null && !address.isBlank()) sb.append(address.trim());
        if (city != null && !city.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(city.trim());
        }
        return sb.toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record NominatimResult(String lat, String lon, String display_name) {}
}
