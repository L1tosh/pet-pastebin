package org.example.pastebin.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.checkerframework.common.aliasing.qual.Unique;

import java.time.LocalDateTime;

@Data
public class PostDTO {
    @NotNull(message = "can't be empty")
    @Column(name = "title")
    @Size(min = 3, message = "size should be more then 3 chars")
    private String title;

    @NotNull(message = "can't be empty")
    @Column(name = "text", unique = true)
    private String text;

    private String hash;

    @FutureOrPresent(message = "should be more then now")
    @NotNull(message = "can't be empty")
    @Temporal(value = TemporalType.DATE)
    private LocalDateTime timeToDelete;
}
