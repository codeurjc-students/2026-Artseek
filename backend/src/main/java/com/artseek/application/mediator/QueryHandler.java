package com.artseek.application.mediator;

public interface QueryHandler<Q extends Query<R>, R> {

    Class<Q> queryType();

    R handle(Q query);
}