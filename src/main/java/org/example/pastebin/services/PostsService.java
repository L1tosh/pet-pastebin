package org.example.pastebin.services;

import lombok.AllArgsConstructor;
import org.example.pastebin.model.Post;
import org.example.pastebin.repositories.PostsRepository;
import org.example.pastebin.utill.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class PostsService {

    private final PostsRepository postsRepository;
    private final GoogleCloudService googleCloudService;


    public Post getPostByHash(String hash) {
        return retrievePost(postsRepository.findByText(hash));
    }

    @Transactional
    public String createPost(Post post) {
        String hash = generateAndUploadHash(post);
        post.setText(hash);
        post.setCreatedDate(LocalDateTime.now());
        postsRepository.save(post);
        return hash;
    }

    @Transactional
    public String updatePost(String hash, Post post) {
        Post postToUpdate = postsRepository.findByText(hash).orElseThrow(() ->
                new NotFoundException("Post not found"));

        googleCloudService.deleteFile(postToUpdate.getText());

        String newHash = generateAndUploadHash(post);

        postToUpdate.setTitle(post.getTitle());
        postToUpdate.setText(newHash);

        postsRepository.save(postToUpdate);

        return newHash;
    }

    @Transactional
    public void deletePost(String hash) {
        Post post = postsRepository.findByText(hash).orElseThrow(() ->
                new NotFoundException("Post not found"));

        googleCloudService.deleteFile(hash);
        postsRepository.delete(post);
    }

    private Post retrievePost(Optional<Post> post) {
        if (post.isEmpty())
            throw new NotFoundException("Post not found");

        Post postToFill = post.get();

        postToFill.setText(googleCloudService.downloadFile(postToFill.getText()));

        return postToFill;
    }

    private String generateAndUploadHash(Post post) {
        String hash = googleCloudService.generateHash(post.getText());
        googleCloudService.uploadFile(hash, post.getText());
        return hash;
    }
}
