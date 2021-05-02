package main;

import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Tardis<PastState, FutureState, Output> {
    public static class Result<P, F, R> {
        private final Supplier<P> newPastState;
        private final Supplier<F> newFutureState;
        private final Supplier<R> result;

        public Result(Supplier<P> newPastState, Supplier<F> newFutureState, Supplier<R> result) {
            this.newPastState = newPastState;
            this.newFutureState = newFutureState;
            this.result = result;
        }

        public Supplier<P> getNewPastState() {
            return newPastState;
        }

        public Supplier<F> getNewFutureState() {
            return newFutureState;
        }

        public Supplier<R> getResult() {
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Result<?, ?, ?> result1 = (Result<?, ?, ?>) o;
            return newPastState.equals(result1.newPastState) && newFutureState.equals(result1.newFutureState) && result.equals(result1.result);
        }

        @Override
        public int hashCode() {
            return Objects.hash(newPastState, newFutureState, result);
        }

        @Override
        public String toString() {
            return "Result{" +
                    "newPastState=" + newPastState.get().toString() +
                    ", newFutureState=" + newFutureState.get().toString() +
                    ", result=" + result.get().toString() +
                    '}';
        }
    }

    private final BiFunction<Supplier<PastState>, Supplier<FutureState>, Result<PastState, FutureState, Output>> runner;

    private Tardis(BiFunction<Supplier<PastState>, Supplier<FutureState>, Result<PastState, FutureState, Output>> runner){
        this.runner = runner;
    }

    public Result<PastState, FutureState, Output> run(Supplier<PastState> past, Supplier<FutureState> future) {
        return runner.apply(past, future);
    }

    public static <P, F> Tardis<P, F, Void> set(Supplier<P> past, Supplier<F> future) {
        return new Tardis<>((ignoredPast, ignoredFuture) -> new Result<>(past, future, () -> null));
    }

    public static <P, F, U> Tardis<P, F, U> pure(Supplier<U> value) {
        return new Tardis<>((past, future) -> new Result<>(past, future, value));
    }

    public static <P, F> Tardis<P, F, P> getPast() {
        return new Tardis<>((past, future) -> new Result<>(past, future, past));
    }

    public static <P, F> Tardis<P, F, F> getFuture() {
        return new Tardis<>((past, future) -> new Result<>(past, future, future));
    }

    public <U> Tardis<PastState, FutureState, U> map(Function<? super Output, U> mapping) {
        return new Tardis<>(runner.andThen(result ->
                new Result<>(result.getNewPastState(), result.getNewFutureState(), () -> mapping.apply(result.getResult().get()))));
    }

    public <U> Tardis<PastState, FutureState, U> bind(Function<? super Output, Tardis<PastState, FutureState, U>> mapping) {
        return new Tardis<>((past, future) -> {
            AtomicReference<PastState> state1Past = new AtomicReference<>();
            Result<PastState, FutureState, Output> result0 = runner.apply(state1Past::get, future);
            Result<PastState, FutureState, U> result1 = mapping.apply(result0.getResult().get()).run(past, result0.getNewFutureState());
            state1Past.set(result1.getNewPastState().get());
            return new Result<>(result0.getNewPastState(), result1.getNewFutureState(), result1.getResult());
        });
    }

    public static <P, F, T, U> Tardis<P, F, LinkedList<U>> traverseList(Function<? super T, Tardis<P, F, U>> mapping, LinkedList<T> list) {
        return new Tardis<>((past, future) -> {
            Tardis<P, F, LinkedList<U>> result = Tardis.pure(LinkedList::new);
            for (T element : list) {
                result = result.bind(l -> mapping.apply(element).map(e -> {
                    l.add(e);
                    return l;
                }));
            }
            return result.run(past, future);
        });
    }
}
