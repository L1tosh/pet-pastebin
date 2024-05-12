package org.example.pastebin.services;

import lombok.AllArgsConstructor;
import org.example.pastebin.model.Post;
import org.example.pastebin.repositories.PostsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        postsRepository.save(post);
    }
}
