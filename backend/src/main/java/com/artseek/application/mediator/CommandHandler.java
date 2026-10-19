package com.artseek.application.mediator;

public interface CommandHandler<C extends Command<R>, R> {

    Class<C> commandType();

    R handle(C command);
}