package org.example.pastebin.services;

import lombok.AllArgsConstructor;
import org.example.pastebin.model.Person;
import org.example.pastebin.model.Post;
import org.example.pastebin.model.PostAccess;
import org.example.pastebin.repositories.PostAccessRepository;
import org.example.pastebin.repositories.PostsRepository;
import org.example.pastebin.utill.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class PostsService {

    private final PostsRepository postsRepository;
    private final PostAccessRepository postAccessRepository;
    private final GoogleCloudService googleCloudService;

    public Post getPostByHash(String hash) {
        return retrievePost(postsRepository.findByHash(hash));
    }
    public List<Post> getPostsAccessibleByPerson(Person person) {
        return postAccessRepository.findByPerson(person).stream()
                .map(PostAccess::getPost)
                .collect(Collectors.toList());
    }
    public List<Post> getPostsByPerson(Person person) {
        return postsRepository.findByOwner(person);
    }

    @Transactional
    public String createPost(Post post) {
        String hash = generateAndUploadHash(post);

        post.setHash(hash);
        post.setText(post.getText().substring(0, Math.min(128, post.getText().length())) + "...");
        post.setCreatedDate(LocalDateTime.now());

        postsRepository.save(post);

        return hash;
    }

    @Transactional
    public String updatePost(String hash, Post post) {
        Post postToUpdate = postsRepository.findByHash(hash).orElseThrow(() ->
                new NotFoundException("post not found"));

        googleCloudService.deleteFile(postToUpdate.getHash());

        String newHash = generateAndUploadHash(post);

        postToUpdate.setHash(newHash);
        postToUpdate.setTitle(post.getTitle());
        postToUpdate.setText(post.getText().substring(0, Math.min(128, post.getText().length())) + "...");

        postsRepository.save(postToUpdate);

        return newHash;
    }

    @Transactional
    public void deletePost(String hash) {
        Post post = postsRepository.findByHash(hash).orElseThrow(() ->
                new NotFoundException("post not found"));

        googleCloudService.deleteFile(hash);
        postsRepository.delete(post);
    }

    @Transactional
    public void grantAccessToPost(Person person, String postHash) {
        Post post = postsRepository.findByHash(postHash).orElseThrow(
                () -> new NotFoundException("post now found"));

        if (person.equals(post.getOwner()))
            throw new RuntimeException("sending post to owner");

        PostAccess postAccess = new PostAccess();

        postAccess.setPost(post);
        postAccess.setPerson(person);

        postAccessRepository.save(postAccess);
    }

    public Post retrievePost(Optional<Post> post) {
        if (post.isEmpty())
            throw new NotFoundException("post not found");

        Post postToFill = post.get();

        postToFill.setText(googleCloudService.downloadFile(postToFill.getHash()));

        return postToFill;
    }

    private String generateAndUploadHash(Post post) {
        String hash = googleCloudService.generateHash(post.getText());
        googleCloudService.uploadFile(hash, post.getText());
        return hash;
    }


}
