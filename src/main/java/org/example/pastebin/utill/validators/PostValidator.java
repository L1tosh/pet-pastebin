package org.example.pastebin.utill.validators;

import lombok.AllArgsConstructor;
import org.example.pastebin.model.Post;
import org.example.pastebin.repositories.PostsRepository;
import org.example.pastebin.services.GoogleCloudService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@AllArgsConstructor
public class PostValidator implements Validator {

    private final PostsRepository postsRepository;
    private final GoogleCloudService googleCloudService;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(Post.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Post post = (Post) target;

        String hash = googleCloudService.generateHash(post.getText());

        if (postsRepository.findByHash(hash).isPresent()) {
            errors.rejectValue("text", "","post with the same content exist");
        }

    }
}
