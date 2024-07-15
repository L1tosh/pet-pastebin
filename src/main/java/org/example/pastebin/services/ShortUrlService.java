package org.example.pastebin.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.pastebin.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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

    @Value("${service.generator-short-url.username}") private String username;
    @Value("${service.generator-short-url.password}") private String password;

    private static final String TOKEN_KEY = "jwtToken";

    @Value("${service.url.create}") private String SHORT_URL_CREATE;
    @Value("${service.url.get}") private String SHORT_URL_GET;
    @Value("${service.url.delete}") private String SHORT_URL_DELETE;
    @Value("${service.url.login}") private String SHORT_URL_LOGIN;


    public String createShortUrl(String key) {
        var entity = new HttpEntity<>(key, getDefaultHeaders());
        var response =
                restTemplate.exchange(SHORT_URL_CREATE, HttpMethod.POST, entity, String.class);

        return getResponseBody(response);
    }

    public String getHash(String key) {
        var entity = new HttpEntity<>(getDefaultHeaders());
        var response =
                restTemplate.exchange(SHORT_URL_GET, HttpMethod.GET, entity, String.class, key);

        return getResponseBody(response);
    }

    public void deleteShortUrl(String key) {
        var entity = new HttpEntity<>(key, getDefaultHeaders());
        try {
            restTemplate.exchange(SHORT_URL_DELETE, HttpMethod.DELETE, entity, Void.class);
        } catch (HttpClientErrorException.NotFound e) {
            System.out.println("Short utl not found or not exist");
        }
    }

    private static <T> T getResponseBody(ResponseEntity<T> response) {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new NotFoundException("Resource not found");
        }
    }

    private String login() {
        Map<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);

        var response =
                restTemplate.postForEntity(SHORT_URL_LOGIN, request, String.class);


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
    private static class LoginResponse {
        @JsonProperty("accessToken")
        private String accessToken;
    }
}
