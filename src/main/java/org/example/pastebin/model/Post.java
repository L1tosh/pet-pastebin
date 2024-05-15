package org.example.pastebin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Can't be empty")
    @Column(name = "title")
    private String title;

    @NotNull(message = "Can't be empty")
    @Column(name = "text")
    private String text;

    @ManyToOne
    @JoinColumn(name = "owner", referencedColumnName = "id")
    private Person owner;

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", owner=" + owner +
                '}';
    }
}
