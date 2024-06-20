package org.example.pastebin.exceptions;

public class IllegalAccessAttemptException extends RuntimeException {
    public IllegalAccessAttemptException(String message) {
        super(message);
    }
}
