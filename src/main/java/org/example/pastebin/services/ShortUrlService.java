package org.example.pastebin.services;

import lombok.AllArgsConstructor;
import org.example.pastebin.utill.exceptions.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class ShortUrlService {

    private final RestTemplate restTemplate;

    private static final String SHORT_URL_CREATE = "http://localhost:8081/api/create";
    private static final String SHORT_URL_GET = "http://localhost:8081/api/get?shortUrl={key}";
    private static final String SHORT_URL_DELETE = "http://localhost:8081/api/delete";

    public String createShortUrl(String key) {
        return postForEntity(SHORT_URL_CREATE, key, String.class);
    }

    public String getHash(String key) {
        return getForEntity(SHORT_URL_GET, String.class, key);
    }

    public Boolean deleteShortUrl(String key) {
        return postForEntity(SHORT_URL_DELETE, key, Boolean.class);
    }

    private <T> T getForEntity(String url, Class<T> responseType, Object... uriVariables) {
        ResponseEntity<T> response = restTemplate.getForEntity(url, responseType, uriVariables);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new NotFoundException("Resource not found for URL: " + url);
        }
    }

    private <T> T postForEntity(String url, Object request, Class<T> responseType) {
        ResponseEntity<T> response = restTemplate.postForEntity(url, request, responseType);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new NotFoundException("Resource not found for URL: " + url);
        }
    }
}
