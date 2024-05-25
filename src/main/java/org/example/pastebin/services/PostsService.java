package org.example.pastebin.services;

import lombok.AllArgsConstructor;
import org.example.pastebin.model.Person;
import org.example.pastebin.model.Post;
import org.example.pastebin.model.PostAccess;
import org.example.pastebin.repositories.PostAccessRepository;
import org.example.pastebin.repositories.PostsRepository;
import org.example.pastebin.utill.exceptions.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class PostsService {

    private final PostsRepository postsRepository;
    private final PostAccessRepository postAccessRepository;
    private final GoogleCloudService googleCloudService;
    private final RestTemplate restTemplate;

    public List<Post> getPostsByPerson(Person person) {
        return postsRepository.findByOwner(person);
    }

    public Post findPostByHashOrThrow(String hash) {
        return postsRepository.findByHash(hash).orElseThrow(() ->
                new NotFoundException("Post not found with hash: " + hash));
    }

    public Post getPostByHash(String hash) {
        try {
            return retrievePost(findPostByHashOrThrow(hash));
        } catch (NotFoundException e) {
            Long postId = getPostIdFromShortUrlService(hash);
            return postsRepository.findById(postId)
                    .map(this::retrievePost)
                    .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        }
    }

    public List<Post> getPostsAccessibleByPerson(Person person) {
        return postAccessRepository.findByPerson(person).stream()
                .map(PostAccess::getPost)
                .collect(Collectors.toList());
    }

    @Transactional
    public String createPost(Post post) {
        String hash = generateAndUploadHash(post);

        post.setHash(hash);
        post.setText(post.getText().substring(0, Math.min(128, post.getText().length())));
        post.setCreatedDate(LocalDateTime.now());

        postsRepository.save(post);

        return hash;
    }

    @Transactional
    public String updatePost(String hash, Post post) {
        Post postToUpdate = findPostByHashOrThrow(hash);

        googleCloudService.deleteFile(postToUpdate.getHash());

        String newHash = generateAndUploadHash(post);

        postToUpdate.setHash(newHash);
        postToUpdate.setTitle(post.getTitle());
        postToUpdate.setText(post.getText().substring(0, Math.min(128, post.getText().length())));

        postsRepository.save(postToUpdate);

        return newHash;
    }

    @Transactional
    public void deletePost(String hash) {
        Post post = findPostByHashOrThrow(hash);

        deleteShortUrl(post.getId());
        googleCloudService.deleteFile(hash);
        postsRepository.delete(post);
    }

    @Transactional
    public void grantAccessToPost(Person person, String hash) {
        Post post = findPostByHashOrThrow(hash);

        if (person.equals(post.getOwner()))
            throw new RuntimeException(String.format(
                    "User with id={%d} is trying to send a post to user with id={%d}",
                    person.getId(), post.getOwner().getId()));

        PostAccess postAccess = new PostAccess();

        postAccess.setPost(post);
        postAccess.setPerson(person);

        postAccessRepository.save(postAccess);
    }

    public String createShortUrl(String hash) {
        Post post = findPostByHashOrThrow(hash);
        String shortUrlService = "http://localhost:8081/api/create";
        return postForEntity(shortUrlService, post.getId(), String.class);
    }

    private Long getPostIdFromShortUrlService(String hash) {
        String shortUrlService = "http://localhost:8081/api/get?url={shortUrl}";
        return getForEntity(shortUrlService, Long.class, hash);
    }

    private Boolean deleteShortUrl(Long postId) {
        String shortUrlService = "http://localhost:8081/api/delete";
        return postForEntity(shortUrlService, postId, Boolean.class);
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

    private Post retrievePost(Post postToFill) {
        postToFill.setText(googleCloudService.downloadFile(postToFill.getHash()));
        return postToFill;
    }

    private String generateAndUploadHash(Post post) {
        String hash = googleCloudService.generateHash(post.getText());
        googleCloudService.uploadFile(hash, post.getText());
        return hash;
    }


}
