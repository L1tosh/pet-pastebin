package org.example.pastebin.utill;

import lombok.AllArgsConstructor;
import org.example.pastebin.model.Person;
import org.example.pastebin.services.PeopleService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@AllArgsConstructor
public class PersonValidator implements Validator {

    private final PeopleService peopleService;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(Person.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Person person = (Person) target;

        if (peopleService.getByEmail(person.getEmail()).isPresent())
            errors.rejectValue("email", "email already exist");
    }
}
