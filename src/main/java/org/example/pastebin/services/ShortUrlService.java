package org.example.pastebin.services;

import lombok.RequiredArgsConstructor;
import org.example.pastebin.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
//@PropertySource("classpath:service.properties")
public class ShortUrlService {

    private final RestTemplate restTemplate;
    //private final RedisService<String, String> redisService;

//    @Value("${service.generator-short-url.username}")
//    private String username;
//    @Value("${service.generator-short-url.password}")
//    private String password;

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

    public Boolean deleteShortUrl(String key) {
        return postForEntity(SHORT_URL_DELETE, key, Boolean.class);
    }

//    public String login() {
////        Map<String, String> request = new HashMap<>();
////        request.put("username", username);
////        request.put("password", password);
////
////        var jwtToken = postForEntity(SHORT_URL_LOGIN, request, String.class);
//       // redisService.saveWithTTL(TOKEN_KEY, jwtToken, Duration.ofDays(1).getSeconds());
//        return jwtToken;
//    }

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
        String token =  "ad";//redisService.get(TOKEN_KEY);
        if (token == null) {
           // token = login();
        }
        return token;
    }
}
