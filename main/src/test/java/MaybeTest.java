import main.Maybe;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Function;

class MaybeTest {
    @Test
    void allJustsShouldBeJust() {
        LinkedList<Integer> list = new LinkedList<>(Arrays.asList(1,2, 3));
        Assertions.assertEquals(Maybe.traverseList(Maybe::just, list), Maybe.just(list));
    }

    @Test
    void oneNothingShouldBeNothing() {
        LinkedList<Maybe<Integer>> list = new LinkedList<>(Arrays.asList(Maybe.just(1),Maybe.just(2), Maybe.nothing(), Maybe.just(3)));
        Assertions.assertEquals(Maybe.traverseList(Function.identity(), list), Maybe.nothing());
    }
}