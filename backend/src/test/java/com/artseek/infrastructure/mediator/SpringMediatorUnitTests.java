package com.artseek.infrastructure.mediator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.artseek.application.mediator.Command;
import com.artseek.application.mediator.CommandHandler;
import com.artseek.application.mediator.Query;
import com.artseek.application.mediator.QueryHandler;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class SpringMediatorUnitTests {

    @Test
    void setsUpRegistriesAndRoutesRequestsToTheirMatchingHandlers() {
        SpringMediator mediator = new SpringMediator(
                List.of(
                        new TestCommandHandler<>(GreetingCommand.class, command -> "Hello " + command.name()),
                        new TestCommandHandler<>(DoubleCommand.class, command -> command.value() * 2)),
                List.of(
                        new TestQueryHandler<>(GreetingQuery.class, query -> "Hello " + query.name()),
                        new TestQueryHandler<>(LengthQuery.class, query -> query.text().length())));

        assertThat(mediator.dispatch(new GreetingCommand("Ada"))).isEqualTo("Hello Ada");
        assertThat(mediator.dispatch(new DoubleCommand(21))).isEqualTo(42);
        assertThat(mediator.send(new GreetingQuery("Grace"))).isEqualTo("Hello Grace");
        assertThat(mediator.send(new LengthQuery("Artseek"))).isEqualTo(7);
    }

    @Test
    void allowsSetupWithoutRegisteredHandlers() {
        assertThatCode(() -> new SpringMediator(List.of(), List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsDuplicateCommandHandlersDuringSetup() {
        var first = new TestCommandHandler<>(GreetingCommand.class, GreetingCommand::name);
        var second = new TestCommandHandler<>(GreetingCommand.class, GreetingCommand::name);

        assertThatThrownBy(() -> new SpringMediator(List.of(first, second), List.of()))
                .isInstanceOf(DuplicateHandlerException.class)
                .hasMessage("Multiple command handlers registered for " + GreetingCommand.class.getName());
    }

    @Test
    void rejectsDuplicateQueryHandlersDuringSetup() {
        var first = new TestQueryHandler<>(GreetingQuery.class, GreetingQuery::name);
        var second = new TestQueryHandler<>(GreetingQuery.class, GreetingQuery::name);

        assertThatThrownBy(() -> new SpringMediator(List.of(), List.of(first, second)))
                .isInstanceOf(DuplicateHandlerException.class)
                .hasMessage("Multiple query handlers registered for " + GreetingQuery.class.getName());
    }

    @Test
    void rejectsACommandWithoutARegisteredHandler() {
        SpringMediator mediator = new SpringMediator(
                List.of(new TestCommandHandler<>(GreetingCommand.class, GreetingCommand::name)),
                List.of());

        assertThatThrownBy(() -> mediator.dispatch(new DoubleCommand(21)))
                .isInstanceOf(HandlerNotFoundException.class)
                .hasMessage("No command handler registered for " + DoubleCommand.class.getName());
    }

    @Test
    void rejectsAQueryWithoutARegisteredHandler() {
        SpringMediator mediator = new SpringMediator(
                List.of(),
                List.of(new TestQueryHandler<>(GreetingQuery.class, GreetingQuery::name)));

        assertThatThrownBy(() -> mediator.send(new LengthQuery("Artseek")))
                .isInstanceOf(HandlerNotFoundException.class)
                .hasMessage("No query handler registered for " + LengthQuery.class.getName());
    }

    private record GreetingCommand(String name) implements Command<String> {
    }

    private record DoubleCommand(int value) implements Command<Integer> {
    }

    private record GreetingQuery(String name) implements Query<String> {
    }

    private record LengthQuery(String text) implements Query<Integer> {
    }

    private record TestCommandHandler<C extends Command<R>, R>(
            Class<C> commandType, Function<C, R> action) implements CommandHandler<C, R> {

        @Override
        public R handle(C command) {
            return action.apply(command);
        }
    }

    private record TestQueryHandler<Q extends Query<R>, R>(
            Class<Q> queryType, Function<Q, R> action) implements QueryHandler<Q, R> {

        @Override
        public R handle(Q query) {
            return action.apply(query);
        }
    }
}
