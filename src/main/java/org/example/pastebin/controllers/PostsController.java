package org.example.pastebin.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.pastebin.dto.PostDTO;
import org.example.pastebin.model.Person;
import org.example.pastebin.model.Post;
import org.example.pastebin.services.PeopleService;
import org.example.pastebin.services.PostsService;
import org.example.pastebin.utill.ResponseError;
import org.example.pastebin.utill.exceptions.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/post")
@AllArgsConstructor
public class PostsController {

    private final PostsService postsService;
    private final ModelMapper modelMapper;
    private final PeopleService peopleService;

    @GetMapping("/new")
    public String showNewPostForm(@ModelAttribute("post") PostDTO post) {
        return "posts/create";
    }

    @GetMapping("/{hash}")
    public String showPost(@PathVariable("hash") String hash, Model model) {
        model.addAttribute("post", postsService.getPostByHash(hash));

        return "posts/index";
    }

    @PostMapping
    public String createPost(@ModelAttribute("post") @Valid PostDTO postDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors())
            return "posts/create";

        String hash = postsService.createPost(fillPost(mapDtoToPost(postDTO)));

        return "redirect:/post/" + hash;
    }

    @GetMapping("/{id}/edit")
    public String showEditPostForm(@PathVariable("id") Long id, Model model) {
        Post post = postsService.getPostById(id);

        model.addAttribute("post", mapPostToDto(post));

        return "posts/edit";
    }

    @PostMapping("/{id}/edit")
    public String updatePost(@PathVariable("id") Long id,
                       @ModelAttribute("post") @Valid PostDTO postDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors())
            return "posts/edit";

        postsService.updatePost(id, mapDtoToPost(postDTO));

        return "redirect:/post";
    }


    private Post mapDtoToPost(PostDTO personDTO) {
        return modelMapper.map(personDTO, Post.class);
    }
    private PostDTO mapPostToDto(Post post) {
        return modelMapper.map(post, PostDTO.class);
    }

    private Post fillPost(Post post) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<Person> person = peopleService.getByEmail(auth.getName());

        if (person.isEmpty())
            throw new NotFoundException("User isn't confirm");

        post.setOwner(person.get());

        return post;
    }

    @ExceptionHandler
    private ResponseEntity<ResponseError> handleNotFoundException(NotFoundException exception) {
        ResponseError response = new ResponseError(
                exception.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
