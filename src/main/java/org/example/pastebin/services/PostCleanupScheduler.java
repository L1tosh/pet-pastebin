package org.example.pastebin.services;

import lombok.AllArgsConstructor;
import org.example.pastebin.model.Post;
import org.example.pastebin.repositories.PostsRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Transactional(readOnly = true)
@AllArgsConstructor
public class PostCleanupScheduler {

    private final PostsRepository postsRepository;

    @Transactional
    @Scheduled(cron = "0 */5 * * * *") // Запускать код каждые 5 мин
    public void deleteExpiredPosts() {

        List<Post> posts = postsRepository
                .findByTimeToDeleteBefore(LocalDateTime.now());

        postsRepository.deleteAll(posts);
    }

}
