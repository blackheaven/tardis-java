package main;

import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Function;

public class StrictState<State, Output> {
    public static class Result<S, R> {
        private final S newState;
        private final R result;

        public Result(S newState, R result) {
            this.newState = newState;
            this.result = result;
        }

        public S getNewState() {
            return newState;
        }

        public R getResult() {
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Result<?, ?> result1 = (Result<?, ?>) o;
            return newState.equals(result1.newState) && result.equals(result1.result);
        }

        @Override
        public int hashCode() {
            return Objects.hash(newState, result);
        }
    }
    private final Function<State, Result<State, Output>> runner;

    private StrictState(Function<State, Result<State, Output>> runner){
        this.runner = runner;
    }

    public Result<State, Output> run(State start) {
        return runner.apply(start);
    }

    public Output eval(State start) {
        return run(start).getResult();
    }

    public State exec(State start) {
        return run(start).getNewState();
    }

    public static <T> StrictState<T, Void> set(T value) {
        return new StrictState<>(ignored -> new Result<>(value, null));
    }

    public static <T, U> StrictState<T, U> pure(U value) {
        return new StrictState<>(state -> new Result<>(state, value));
    }

    public static <T> StrictState<T, T> get() {
        return new StrictState<>(state -> new Result<>(state, state));
    }

    public <U> StrictState<State, U> map(Function<? super Output, ? extends U> mapping) {
        return new StrictState<>(runner.andThen(result -> new Result<>(result.getNewState(), mapping.apply(result.getResult()))));
    }

    public <T> StrictState<State, T> bind(Function<? super Output, StrictState<State, T>> mapping) {
        return new StrictState<>(oldState -> {
            Result<State, Output> result = runner.apply(oldState);
            return mapping.apply(result.getResult()).run(result.getNewState());
        });
    }

    public static <State, T, U> StrictState<State, LinkedList<U>> traverseList(Function<? super T, StrictState<State, U>> mapping, LinkedList<T> list) {
        return new StrictState<>(oldState -> {
            StrictState<State, LinkedList<U>> result = StrictState.pure(new LinkedList<U>());
            for (T element : list) {
                result = result.bind(l -> mapping.apply(element).map(e -> {
                    l.add(e);
                    return l;
                }));
            }
            return result.run(oldState);
        });
    }
}
