package org.example.pastebin.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.pastebin.dto.PostDTO;
import org.example.pastebin.model.Person;
import org.example.pastebin.model.Post;
import org.example.pastebin.services.PeopleService;
import org.example.pastebin.services.PostsService;
import org.example.pastebin.services.ShortUrlService;
import org.example.pastebin.utill.validators.PostValidator;
import org.example.pastebin.exceptions.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/post")
@AllArgsConstructor
public class PostsController {

    private final PostsService postsService;
    private final ShortUrlService shortUrlService;
    private final PeopleService peopleService;
    private final PostValidator postValidator;
    private final ModelMapper modelMapper;

    @GetMapping({"", "/"})
    public String showMainPage() {
        return "posts/main";
    }

    @GetMapping("/{hash}")
    public String showPost(@PathVariable("hash") String hash, Principal principal, Model model) {
        Post post = postsService.getPostByHash(hash);
        model.addAttribute("post",  mapPostToDto(post));

        if (principal == null || !principal.getName().equals(post.getOwner().getEmail()))
            return "posts/to-guest";

        return "posts/index";
    }

    @GetMapping("/new")
    public String showNewPostForm(@ModelAttribute("post") PostDTO post) {
        return "posts/create";
    }

    @GetMapping("/{hash}/edit")
    public String showEditPostForm(@PathVariable("hash") String hash, Model model) {
        Post post = postsService.getPostByHash(hash);

        model.addAttribute("post", mapPostToDto(post));

        return "posts/edit";
    }

    @GetMapping("/show")
    public String showPageWithAllAccessPosts(Model model, Principal principal) {
        Optional<Person> person = peopleService.getByEmail(principal.getName());

        if (person.isEmpty())
            throw new NotFoundException("User doesn't authenticate");

        model.addAttribute("myPosts", postsService.getPostsByPerson(person.get()));
        model.addAttribute("sharingPosts", postsService.getPostsAccessibleByPerson(person.get()));

        return "posts/allPosts";
    }

    @PostMapping
    public String createPost(@ModelAttribute("post") @Valid PostDTO postDTO, BindingResult bindingResult) {
        postValidator.validate(mapDtoToPost(postDTO), bindingResult);

        if (bindingResult.hasErrors())
            return "posts/create";

        String hash = postsService.createPost(fillPost(mapDtoToPost(postDTO)));

        return String.format("redirect:/post/%s", hash);
    }

    @PutMapping("/{hash}")
    public String updatePost(@PathVariable("hash") String hash,
                             @ModelAttribute("post") @Valid PostDTO postDTO, BindingResult bindingResult) {
        postValidator.validate(mapDtoToPost(postDTO), bindingResult);

        if (bindingResult.hasErrors())
            return "posts/edit";

        String newHash = postsService.updatePost(hash, mapDtoToPost(postDTO));

        return String.format("redirect:/post/%s", newHash);
    }

    @DeleteMapping("/{hash}")
    public String deletePost(@PathVariable("hash") String hash) {
        postsService.deletePost(hash);
        return "redirect:/post";
    }

    @PostMapping("/{hash}/send")
    public String sendPost(@PathVariable("hash") String hash,
                           @RequestParam("email") String email, Model model) {
        Optional<Person> person = peopleService.getByEmail(email);

        if (person.isEmpty()) {
            model.addAttribute("error", "User with this email not exist");
            return String.format("redirect:/post/%s", hash);
        }

        postsService.grantAccessToPost(person.get(), hash);

        return String.format("redirect:/post/%s", hash);
    }

    @PostMapping("/{hash}/generate")
    public String createShortUrl(@PathVariable("hash") String hash, Model model) {
        model.addAttribute("shortUrl", shortUrlService.createShortUrl(hash));
        return "posts/short-url";
    }


    private Post mapDtoToPost(PostDTO personDTO) {
        return modelMapper.map(personDTO, Post.class);
    }

    private PostDTO mapPostToDto(Post post) {
        return modelMapper.map(post, PostDTO.class);
    }


    // add to post information about authentication user
    private Post fillPost(Post post) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<Person> person = peopleService.getByEmail(auth.getName());

        if (person.isEmpty())
            throw new NotFoundException("User isn't confirm");

        post.setOwner(person.get());

        return post;
    }
}
