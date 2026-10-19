package com.artseek.infrastructure.mediator;

public class DuplicateHandlerException extends RuntimeException {

    public DuplicateHandlerException(String message) {
        super(message);
    }
}