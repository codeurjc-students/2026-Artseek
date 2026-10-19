package com.artseek.infrastructure.mediator;

public class HandlerNotFoundException extends RuntimeException {

    public HandlerNotFoundException(String message) {
        super(message);
    }
}