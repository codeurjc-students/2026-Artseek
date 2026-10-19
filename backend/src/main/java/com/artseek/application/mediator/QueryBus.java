package com.artseek.application.mediator;

public interface QueryBus {
    <R> R send(Query<R> query);
}