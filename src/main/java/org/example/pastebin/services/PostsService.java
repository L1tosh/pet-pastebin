package org.example.pastebin.services;

import lombok.AllArgsConstructor;
import org.example.pastebin.model.Person;
import org.example.pastebin.model.Post;
import org.example.pastebin.model.PostAccess;
import org.example.pastebin.repositories.PostAccessRepository;
import org.example.pastebin.repositories.PostsRepository;
import org.example.pastebin.utill.exceptions.IllegalAccessAttemptException;
import org.example.pastebin.utill.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ShortUrlService shortUrlService;

    private final RedisService<String, Post> redisService;

    private static final long TTL_TIME = 300L;

    public List<Post> getPostsByPerson(Person person) {
        return postsRepository.findByOwner(person);
    }

    public Post findPostByHashOrThrow(String hash) {
        return postsRepository.findByHash(hash).orElseThrow(() ->
                new NotFoundException("Post not found with hash: " + hash));
    }

    public Post getPostByHash(String hash) {
        Post post = redisService.get(hash);

        if (post == null) {
            try {
                post = retrievePost(findPostByHashOrThrow(hash));
            } catch (NotFoundException e) {
                post = retrievePost(findPostByHashOrThrow(shortUrlService.getHash(hash)));
            }
        }

        post.getOwner().setPosts(null); // Prevent lazy loading issues

        redisService.saveWithTTL(hash, post, TTL_TIME);

        return retrievePost(post);
    }

    public List<Post> getPostsAccessibleByPerson(Person person) {
        return postAccessRepository.findByPerson(person).stream()
                .map(PostAccess::getPost)
                .collect(Collectors.toList());
    }

    @Transactional
    public String createPost(Post post) {
        String hash = generateAndUploadHash(post);

        setPostProperties(post, hash);

        postsRepository.save(post);

        return hash;
    }

    @Transactional
    public String updatePost(String hash, Post post) {
        Post postToUpdate = findPostByHashOrThrow(hash);

        googleCloudService.deleteFile(postToUpdate.getHash());

        String newHash = generateAndUploadHash(post);

        postToUpdate.setTitle(post.getTitle());
        postToUpdate.setTimeToDelete(post.getTimeToDelete());

        setPostProperties(postToUpdate, newHash);

        postsRepository.save(postToUpdate);

        return newHash;
    }

    @Transactional
    public void deletePost(String hash) {
        Post post = findPostByHashOrThrow(hash);

        redisService.delete(hash);
        shortUrlService.deleteShortUrl(hash);
        googleCloudService.deleteFile(hash);
        postsRepository.delete(post);
    }

    /**
     * Grants access to a post to a specified person.
     *
     * @param person the person to grant access to
     * @param hash the hash of the post
     */
    @Transactional
    public void grantAccessToPost(Person person, String hash) {
        Post post = findPostByHashOrThrow(hash);

        if (person.equals(post.getOwner())) {
            throw new IllegalAccessAttemptException(String.format(
                    "User with id={%d} is trying to send a post to user with id={%d}",
                    person.getId(), post.getOwner().getId()));
        }

        PostAccess postAccess = new PostAccess();

        postAccess.setPost(post);
        postAccess.setPerson(person);

        postAccessRepository.save(postAccess);
    }

    /**
     * Helper method to retrieve a post's content from Google Cloud.
     *
     * @param postToFill the post to retrieve content for
     * @return the post with its content filled
     */
    private Post retrievePost(Post postToFill) {
        postToFill.setText(googleCloudService.downloadFile(postToFill.getHash()));
        return postToFill;
    }

    private String generateAndUploadHash(Post post) {
        String hash = googleCloudService.generateHash(post.getText());
        googleCloudService.uploadFile(hash, post.getText());
        return hash;
    }

    private String trimText(String text) {
        return text.substring(0, Math.min(128, text.length()));
    }

    private void setPostProperties(Post post, String hash) {
        post.setHash(hash);
        post.setText(trimText(post.getText()));
        post.setCreatedDate(LocalDateTime.now());
    }
    

}
