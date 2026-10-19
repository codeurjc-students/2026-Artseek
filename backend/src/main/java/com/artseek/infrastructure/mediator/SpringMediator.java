package com.artseek.infrastructure.mediator;

import com.artseek.application.mediator.Command;
import com.artseek.application.mediator.CommandHandler;
import com.artseek.application.mediator.Mediator;
import com.artseek.application.mediator.Query;
import com.artseek.application.mediator.QueryHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SpringMediator implements Mediator {

    private final Map<Class<?>, CommandHandler<?, ?>> commandHandlers;
    private final Map<Class<?>, QueryHandler<?, ?>> queryHandlers;

    public SpringMediator(
            List<CommandHandler<?, ?>> commandHandlers,
            List<QueryHandler<?, ?>> queryHandlers) {

        this.commandHandlers = buildCommandRegistry(commandHandlers);
        this.queryHandlers = buildQueryRegistry(queryHandlers);
    }

    @Override
    public <R> R dispatch(Command<R> command) {
        CommandHandler<Command<R>, R> handler = findCommandHandler(command);

        return handler.handle(command);
    }

    @Override
    public <R> R send(Query<R> query) {
        QueryHandler<Query<R>, R> handler = findQueryHandler(query);

        return handler.handle(query);
    }

    @SuppressWarnings("unchecked")
    private <R> CommandHandler<Command<R>, R> findCommandHandler(Command<R> command) {
        CommandHandler<?, ?> handler = commandHandlers.get(command.getClass());

        if (handler == null) {
            throw new HandlerNotFoundException("No command handler registered for "+ command.getClass().getName());
        }

        return (CommandHandler<Command<R>, R>) handler;
    }

    @SuppressWarnings("unchecked")
    private <R> QueryHandler<Query<R>, R> findQueryHandler(Query<R> query) {
        QueryHandler<?, ?> handler = queryHandlers.get(query.getClass());

        if (handler == null) {
            throw new HandlerNotFoundException("No query handler registered for "+ query.getClass().getName());
        }

        return (QueryHandler<Query<R>, R>) handler;
    }

    private static Map<Class<?>, CommandHandler<?, ?>> buildCommandRegistry(List<CommandHandler<?, ?>> handlers) {
        Map<Class<?>, CommandHandler<?, ?>> registry = new HashMap<>();

        for (CommandHandler<?, ?> handler : handlers) {
            CommandHandler<?, ?> existing = registry.put(handler.commandType(), handler);

            if (existing != null) {
                throw new DuplicateHandlerException("Multiple command handlers registered for "+ handler.commandType().getName());
            }
        }

        return Map.copyOf(registry);
    }

    private static Map<Class<?>, QueryHandler<?, ?>> buildQueryRegistry(List<QueryHandler<?, ?>> handlers) {
        Map<Class<?>, QueryHandler<?, ?>> registry = new HashMap<>();

        for (QueryHandler<?, ?> handler : handlers) {
            QueryHandler<?, ?> existing = registry.put(handler.queryType(), handler);

            if (existing != null) {
                throw new DuplicateHandlerException("Multiple query handlers registered for "+ handler.queryType().getName());
            }
        }

        return Map.copyOf(registry);
    }
}
