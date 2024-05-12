package org.example.pastebin.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.pastebin.dto.PersonDTO;
import org.example.pastebin.model.Person;
import org.example.pastebin.services.PeopleService;
import org.example.pastebin.utill.PersonValidator;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class AuthorizationController {

    private final ModelMapper modelMapper;
    private final PeopleService peopleService;
    private final PersonValidator personValidator;

    @GetMapping("/login")
    public String login() {
     return "auth/login";
    }

    @GetMapping("/registration")
    public String registration(@ModelAttribute("person") PersonDTO personDTO) {
        return "auth/registration";
    }

    @PostMapping("/registration")
    public String createNewPerson(@ModelAttribute("person") @Valid PersonDTO personDTO, BindingResult bindingResult) {
        Person person = convertToUser(personDTO);
        personValidator.validate(person, bindingResult);

        if (bindingResult.hasErrors())
            return "auth/registration";

        peopleService.save(person);

        return "redirect:/login";
    }


    private Person convertToUser(PersonDTO personDTO) {
        return modelMapper.map(personDTO, Person.class);
    }
}
