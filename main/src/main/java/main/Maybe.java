package main;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class Maybe<T> {
    private final Optional<T> value;

    private Maybe(Optional<T> value){
        this.value = value;
    }

    public static <U> Maybe<U> pure(U value) {
        return just(value);
    }

    public static <U> Maybe<U> just(U value) {
        return new Maybe<>(Optional.of(value));
    }

    public static <U> Maybe<U> nothing() {
        return new Maybe<>(Optional.empty());
    }

    public <U> Maybe<U> map(Function<? super T, ? extends U> mapping) {
        return new Maybe<>(value.map(mapping));
    }

    public <U> Maybe<U> bind(Function<? super T, Maybe<U>> mapping) {
        return value.map(mapping).orElse(nothing());
    }

    public static <T, U> Maybe<LinkedList<U>> traverseList(Function<? super T, Maybe<U>> mapping, LinkedList<T> list) {
        Maybe<LinkedList<U>> result = just(new LinkedList<>());
        for (T element : list) {
            result = result.bind(l -> mapping.apply(element).map(e -> {
                l.add(e);
                return l;
            }));
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Maybe<?> maybe = (Maybe<?>) o;
        return value.equals(maybe.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
