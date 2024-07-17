/*
 * Copyright © 2019-2024 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.memory;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import win.doyto.query.core.QuerySuffix;

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
}