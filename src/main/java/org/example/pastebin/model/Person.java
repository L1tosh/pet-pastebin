package org.example.pastebin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.pastebin.model.enums.Role;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "people")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true)
    @Email(message = "email should be valid")
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

    @Size(min = 4, max = 100, message = "name should be greater then 2 and less then 100")
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @OneToMany(mappedBy = "owner")
    private List<Post> posts;
}
