package org.example.pastebin.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "post_access")
public class PostAccess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;
}
