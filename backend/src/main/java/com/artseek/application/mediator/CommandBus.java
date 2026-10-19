package com.artseek.application.mediator;

public interface CommandBus {
    <R> R dispatch(Command<R> command);
}
