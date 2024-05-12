package org.example.pastebin.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.pastebin.dto.PostDTO;
import org.example.pastebin.model.Person;
import org.example.pastebin.model.Post;
import org.example.pastebin.services.PeopleService;
import org.example.pastebin.services.PostsService;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.attribute.UserPrincipalNotFoundException;
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


    private Post convertToPost(PostDTO personDTO) {
        return modelMapper.map(personDTO, Post.class);
    }


    private Post fillPost(Post post) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<Person> person = peopleService.getByEmail(auth.getName());

        if (person.isEmpty())
            throw new UsernameNotFoundException("User isn't confirm");

        post.setOwner(person.get());

        return post;
    }
}
