package org.example.backend.exception;

public class TMDBMovieNotFoundException extends RuntimeException {
    public TMDBMovieNotFoundException(String message) {
        super(message);
    }
}