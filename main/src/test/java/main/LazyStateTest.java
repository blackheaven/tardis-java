package main;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Supplier;

class LazyStateTest {
    @Test
    void test() {
        Function<Integer, LazyState<Integer, Integer>> inc =
                increment -> {
                    LazyState<Integer, Integer> get = LazyState.get();
                    return get.bind(((Function<Integer, Function<Integer, LazyState<Integer, Integer>>>) increment1 -> (Integer state) -> {
                        Supplier<Integer> newVal = () -> state + increment1;
                        return LazyState.set(newVal)
                                .bind(ignored -> LazyState.pure(newVal));
                    }).apply(increment));
                };
        LinkedList<Integer> input = new LinkedList<>(Arrays.asList(1, 2, 1));
        LinkedList<Integer> output = new LinkedList<>(Arrays.asList(39, 41, 42));
        Assertions.assertEquals(LazyState.traverseList(inc, input).run(() -> 38),
                new LazyState.Result<>(() -> 42, () -> output));
    }

}