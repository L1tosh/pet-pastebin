package org.example.pastebin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.checkerframework.common.aliasing.qual.Unique;
import org.hibernate.annotations.TimeZoneColumn;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

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

    @Unique
    @NotNull(message = "Can't be empty")
    @Column(name = "hash")
    private String hash;

    @NotNull(message = "Can't be empty")
    @Column(name = "text")
    private String text;

    @TimeZoneColumn(name = "created_date")
    private LocalDateTime createdDate;

    @FutureOrPresent
    @NotNull(message = "Can't be empty")
    @TimeZoneColumn(name = "time_to_delete")
    private LocalDateTime timeToDelete;

    @ManyToOne
    @JoinColumn(name = "owner", referencedColumnName = "id")
    private Person owner;
}
