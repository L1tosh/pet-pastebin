package org.example.pastebin.repositories;

import org.example.pastebin.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostsRepository extends JpaRepository<Post, Long> {
}
