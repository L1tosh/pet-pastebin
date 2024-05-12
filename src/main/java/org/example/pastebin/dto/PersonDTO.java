package org.example.pastebin.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class PersonDTO {
    @Column(name = "email", unique = true)
    @Email(message = "email should be valid")
    @NotNull(message = "name can't be empty")
    private String email;

    @Column(name = "first_name")
    @NotNull(message = "name can't be empty")
    @Size(min = 2, max = 100, message = "name should be greater then 2 and less then 100")
    private String firstName;

    @Column(name = "last_name")
    @NotNull(message = "lastname can't be empty")
    @Size(min = 2, max = 100, message = "name should be greater then 2 and less then 100")
    private String lastName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateOfBirth;

    @Size(min = 4, max = 100, message = "name should be greater then 4 and less then 100")
    private String password;
}
