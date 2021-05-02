package main;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class StrictStateTest {
    @Test
    void test() {
        Function<Integer, StrictState<Integer, Integer>> inc =
                increment -> {
                    StrictState<Integer, Integer> get = StrictState.get();
                    return get.bind(((Function<Integer, Function<Integer, StrictState<Integer, Integer>>>) increment1 -> (Integer state) -> {
                        Integer newVal = state + increment1;
                        return StrictState.set(newVal)
                                .bind(ignored -> StrictState.pure(newVal));
                    }).apply(increment));
                };
        LinkedList<Integer> input = new LinkedList<>(Arrays.asList(1, 2, 1));
        LinkedList<Integer> output = new LinkedList<>(Arrays.asList(39, 41, 42));
        Assertions.assertEquals(StrictState.traverseList(inc, input).run(38),
                new StrictState.Result<>(42, output));
    }

}