package org.example.pastebin.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostDTO {
    @NotNull(message = "Can't be empty")
    @Column(name = "title")
    private String title;

    @NotNull(message = "Can't be empty")
    @Column(name = "text")
    private String text;
}
