package main;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Supplier;

class TardisTest {
    @Test
    void test() {
        Function<Integer, Tardis<Integer, Integer, String>> paginate =
                page -> {
                    Tardis<Integer, Integer, Integer> getPast = Tardis.getPast();
                    Tardis<Integer, Integer, Integer> getFuture = Tardis.getFuture();
                    return getPast.bind(past ->
                            getFuture.bind(future -> {
                                Integer newPast = Math.min(past, page);
                                Integer newFuture = Math.max(future, page);
                                return Tardis.set(() -> newPast, () -> newFuture)
                                        .bind(result -> Tardis.pure(() -> newPast + "[" + page + "]" + newFuture));
                            }));
                };
        LinkedList<Integer> input = new LinkedList<>(Arrays.asList(7, 42, 5));
        LinkedList<String> output = new LinkedList<>(Arrays.asList("5[7]42", "5[42]42", "5[5]42"));
        Assertions.assertEquals(new Tardis.Result<>(() -> 5, () -> 42, () -> output),
                Tardis.traverseList(paginate, input).run(() -> Integer.MAX_VALUE, () -> Integer.MIN_VALUE));
    }

}