package org.example.pastebin.services;

import lombok.AllArgsConstructor;
import org.example.pastebin.model.Post;
import org.example.pastebin.repositories.PostsRepository;
import org.example.pastebin.utill.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class PostsService {

    private final PostsRepository postsRepository;

    public List<Post> getAll() {
        return postsRepository.findAll();
    }

    @Transactional
    public void create(Post post) {
        post.setCreatedDate(LocalDateTime.now());
        postsRepository.save(post);
    }

    public Optional<Post> getById(Long id) {
        return postsRepository.findById(id);
    }

    @Transactional
    public void edit(Long id, Post post) {
        Optional<Post> currentPost = postsRepository.findById(id);
        
        if (currentPost.isEmpty())
            throw new NotFoundException("Post not found");
        
        Post postToEdit = currentPost.get();

        postToEdit.setTitle(post.getTitle());
        postToEdit.setText(post.getText());

        postsRepository.save(postToEdit);
    }
}
