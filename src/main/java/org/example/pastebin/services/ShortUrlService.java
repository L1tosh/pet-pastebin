package org.example.pastebin.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.pastebin.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:service.properties")
public class ShortUrlService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final RedisService<String, String> redisServiceShortUrl;

    @Value("${service.generator-short-url.username}")
    private String username;
    @Value("${service.generator-short-url.password}")
    private String password;

    private static final String TOKEN_KEY = "jwtToken";

    private static final String SHORT_URL_CREATE = "http://localhost:8081/api/create";
    private static final String SHORT_URL_GET = "http://localhost:8081/api/get?shortUrl={key}";
    private static final String SHORT_URL_DELETE = "http://localhost:8081/api/delete";
    private static final String SHORT_URL_LOGIN = "http://localhost:8081/api/auth/login";

    public String createShortUrl(String key) {
        return postForEntity(SHORT_URL_CREATE, key, String.class);
    }

    public String getHash(String key) {
        return getForEntity(SHORT_URL_GET, String.class, key);
    }

    public void deleteShortUrl(String key) {
        var entity = new HttpEntity<>(key, getDefaultHeaders());
        restTemplate.postForLocation(SHORT_URL_DELETE, entity);
    }

    public String login() {
        Map<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);

        ResponseEntity<String> response = restTemplate.postForEntity(SHORT_URL_LOGIN, request, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            LoginResponse loginResponse;
            try {
                loginResponse = objectMapper.readValue(response.getBody(), LoginResponse.class);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing login response", e);
            }

            String token = loginResponse.getAccessToken();
            redisServiceShortUrl.saveWithTTL(TOKEN_KEY, token, Duration.ofDays(1).getSeconds());
            return token;
        } else {
            throw new NotFoundException("Resource not found for URL");
        }
    }

    private <T> T getForEntity(String url, Class<T> responseType, Object... uriVariables) {
        var entity = new HttpEntity<>(getDefaultHeaders());
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType, uriVariables);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new NotFoundException("Resource not found for URL: " + url);
        }
    }

    private <T> T postForEntity(String url, Object request, Class<T> responseType) {
        var entity = new HttpEntity<>(request, getDefaultHeaders());
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new NotFoundException("Resource not found for URL: " + url);
        }
    }

    private HttpHeaders getDefaultHeaders() {
        var headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getToken());
        return headers;
    }

    private String getToken() {
        String token = redisServiceShortUrl.get(TOKEN_KEY);
        if (token == null) {
            token = login();
        }
        return token;
    }

    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoginResponse {
        @JsonProperty("accessToken")
        private String accessToken;
    }
}
