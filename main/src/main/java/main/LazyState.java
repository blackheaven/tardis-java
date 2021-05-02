package main;

import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class LazyState<State, Output> {
    public static class Result<S, R> {
        private final Supplier<S> newState;
        private final Supplier<R> result;

        public Result(Supplier<S> newState, Supplier<R> result) {
            this.newState = newState;
            this.result = result;
        }

        public Supplier<S> getNewState() {
            return newState;
        }

        public Supplier<R> getResult() {
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Result<?, ?> result1 = (Result<?, ?>) o;
            return newState.get().equals(result1.newState.get()) && result.get().equals(result1.result.get());
        }

        @Override
        public int hashCode() {
            return Objects.hash(newState.get(), result.get());
        }
    }
    private final Function<Supplier<State>, Result<State, Output>> runner;

    private LazyState(Function<Supplier<State>, Result<State, Output>> runner){
        this.runner = runner;
    }

    public Result<State, Output> run(Supplier<State> start) {
        return runner.apply(start);
    }

    public Supplier<Output> eval(Supplier<State> start) {
        return run(start).getResult();
    }

    public Supplier<State> exec(Supplier<State> start) {
        return run(start).getNewState();
    }

    public static <T> LazyState<T, Void> set(Supplier<T> value) {
        return new LazyState<>(ignored -> new Result<>(value, () -> null));
    }

    public static <T, U> LazyState<T, U> pure(Supplier<U> value) {
        return new LazyState<>(state -> new Result<>(state, value));
    }

    public static <T> LazyState<T, T> get() {
        return new LazyState<>(state -> new Result<>(state, state));
    }

    public <U> LazyState<State, U> map(Function<? super Output, U> mapping) {
        return new LazyState<>(runner.andThen(result -> new Result<>(() -> result.getNewState().get(), () -> mapping.apply(result.getResult().get()))));
    }

    public <T> LazyState<State, T> bind(Function<? super Output, LazyState<State, T>> mapping) {
        return new LazyState<>(oldState -> {
            Result<State, Output> result = runner.apply(oldState);
            return mapping.apply(result.getResult().get()).run(result.getNewState());
        });
    }

    public static <State, T, U> LazyState<State, LinkedList<U>> traverseList(Function<? super T, LazyState<State, U>> mapping, LinkedList<T> list) {
        return new LazyState<>(oldState -> {
            LazyState<State, LinkedList<U>> result = LazyState.pure(LinkedList::new);
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
