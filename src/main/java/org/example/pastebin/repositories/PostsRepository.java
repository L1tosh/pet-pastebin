package org.example.pastebin.repositories;

import org.example.pastebin.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostsRepository extends JpaRepository<Post, Long> {
    List<Post> findByTimeToDeleteBefore(LocalDateTime localDateTime);
    Optional<Post> findByText(String text);
}
