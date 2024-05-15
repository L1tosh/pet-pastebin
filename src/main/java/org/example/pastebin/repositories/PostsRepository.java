package org.example.pastebin.repositories;

import org.example.pastebin.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostsRepository extends JpaRepository<Post, Long> {
    List<Post> findByTimeToDeleteBefore(LocalDateTime localDateTime);
}
