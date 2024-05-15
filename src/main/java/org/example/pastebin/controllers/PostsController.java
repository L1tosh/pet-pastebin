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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @GetMapping
    public String showALl(Model model) {
        model.addAttribute("posts", postsService.getAll());
        return "posts/index";
    }

    @GetMapping("/new")
    public String newPost(@ModelAttribute("post") PostDTO post) {
        return "posts/create";
    }

    @PostMapping
    public String create(@ModelAttribute("post") @Valid PostDTO postDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors())
            return "posts/create";

        postsService.create(fillPost(convertToPost(postDTO)));

        return "redirect:/post";
    }

    @GetMapping("/{id}/edit")
    public String change(@PathVariable("id") Long id, Model model) {
        Optional<Post> post = postsService.getById(id);

        if (post.isEmpty())
            throw new NotFoundException("Post not found");

        model.addAttribute("post", convertToPostDTP(post.get()));

        return "posts/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id,
                       @ModelAttribute("post") @Valid PostDTO postDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors())
            return "posts/edit";

        System.out.println(postDTO);

        postsService.edit(id, convertToPost(postDTO));

        return "redirect:/post";
    }


    private Post convertToPost(PostDTO personDTO) {
        return modelMapper.map(personDTO, Post.class);
    }
    private PostDTO convertToPostDTP(Post post) {
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
    private ResponseEntity<ResponseError> handleException(NotFoundException exception) {
        ResponseError response = new ResponseError(
                exception.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
