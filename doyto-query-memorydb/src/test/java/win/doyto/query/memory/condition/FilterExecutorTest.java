package win.doyto.query.memory.condition;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import win.doyto.query.core.QuerySuffix;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FilterExecutorTest
 *
 * @author f0rb on 2024/7/17
 */
class FilterExecutorTest {
    @ParameterizedTest
    @CsvSource(value = {
            "Eq, hello, hello, 5, 6",
            "Ne, 5, 6, hello, hello",
            "Not, 5, 6, hello, hello",
            "Start, username, user, username, na",
            "NotStart, username, name, username, user",
            "End, username, name, username, na",
            "NotEnd, username, na, username, name",
            "Contain, username, na, username, an",
            "NotContain, username, an, username, na"
    })
    void supportQuerySuffix(QuerySuffix querySuffix, Object t1, Object t2, Object f1, Object f2) {
        Matcher matcher = FilterExecutor.get(querySuffix);
        assertTrue(matcher.match(t1, t2));
        assertFalse(matcher.match(f1, f2));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "Eq, 2, 2, 5, 6",
            "Ne, 5, 6, 6, 6",
            "Gt, 9, 6, 5, 6",
            "Ge, 6, 6, 5, 6",
            "Lt, 5, 6, 10, 6",
            "Le, 6, 6, 10, 6",
    })
    void supportQuerySuffix(QuerySuffix querySuffix, Integer t1, Integer t2, Integer f1, Integer f2) {
        Matcher matcher = FilterExecutor.get(querySuffix);
        assertTrue(matcher.match(t1, t2));
        assertFalse(matcher.match(f1, f2));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "In, 2, 1 2 3, 5, 1 2 3",
            "NotIn, 5, 1 2 3, 1, 1 2 3",
    })
    void supportQuerySuffix(QuerySuffix querySuffix, String t1, String t2, String f1, String f2) {
        Matcher matcher = FilterExecutor.get(querySuffix);
        assertTrue(matcher.match(t1, Arrays.asList(t2.split(" "))));
        assertFalse(matcher.match(f1, Arrays.asList(f2.split(" "))));
    }
}