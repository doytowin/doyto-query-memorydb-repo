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

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.core.QuerySuffix;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static win.doyto.query.core.QuerySuffix.*;

/**
 * FilterExecutor
 *
 * @author f0rb on 2021-12-10
 */
@SuppressWarnings("unchecked")
@UtilityClass
class FilterExecutor {

    static final Map<QuerySuffix, Matcher> map = new EnumMap<>(QuerySuffix.class);

    static {
        map.put(Like, new LikeMatcher());
        map.put(NotLike, new NotLikeMatcher());
        map.put(Start, new StartMatcher());
        map.put(End, new EndMatcher());
        map.put(Contain, new LikeMatcher());
        map.put(NotStart, new NotStartMatcher());
        map.put(NotEnd, new NotEndMatcher());
        map.put(NotContain, new NotLikeMatcher());
        map.put(Null, new NullMatcher());
        map.put(In, (efv, qfv) -> ((Collection<?>) qfv).contains(efv));
        map.put(NotIn, (efv, qfv) -> !((Collection<?>) qfv).contains(efv));
        map.put(Gt, (efv, qfv) -> ((Comparable<Object>) efv).compareTo(qfv) > 0);
        map.put(Lt, (efv, qfv) -> ((Comparable<Object>) efv).compareTo(qfv) < 0);
        map.put(Ge, (efv, qfv) -> ((Comparable<Object>) efv).compareTo(qfv) >= 0);
        map.put(Le, (efv, qfv) -> ((Comparable<Object>) efv).compareTo(qfv) <= 0);
        map.put(Not, (efv, qfv) -> !efv.equals(qfv));
        map.put(Ne, (efv, qfv) -> !efv.equals(qfv));
    }

    static Matcher get(QuerySuffix querySuffix) {
        return map.getOrDefault(querySuffix, (obj, o) -> o.equals(obj));
    }

    static Predicate<Object> build(QuerySuffix querySuffix, Object qfv) {
        if (querySuffix == Rx) {
            Pattern p = Pattern.compile((String) qfv);
            return efv -> efv instanceof String text && p.matcher(text).find();
        }
        Matcher matcher = get(querySuffix);
        return efv -> matcher.match(efv, qfv);
    }

    static class LikeMatcher implements Matcher {
        @Override
        public boolean doMatch(Object efv, Object qfv) {
            return StringUtils.contains(efv.toString(), qfv.toString());
        }

        @Override
        public boolean isComparable(Object efv, Object qfv) {
            return efv instanceof String;
        }
    }

    static class NotLikeMatcher extends LikeMatcher {
        @Override
        public boolean doMatch(Object efv, Object qfv) {
            return !super.doMatch(efv, qfv);
        }
    }

    static class StartMatcher extends LikeMatcher {
        @Override
        public boolean doMatch(Object efv, Object qfv) {
            return StringUtils.startsWith(efv.toString(), qfv.toString());
        }
    }

    static class NotStartMatcher extends LikeMatcher {
        @Override
        public boolean doMatch(Object efv, Object qfv) {
            return !StringUtils.startsWith(efv.toString(), qfv.toString());
        }
    }

    static class EndMatcher extends LikeMatcher {
        @Override
        public boolean doMatch(Object efv, Object qfv) {
            return StringUtils.endsWith(efv.toString(), qfv.toString());
        }
    }

    static class NotEndMatcher extends LikeMatcher {
        @Override
        public boolean doMatch(Object efv, Object qfv) {
            return !StringUtils.endsWith(efv.toString(), qfv.toString());
        }
    }

    static class NullMatcher implements Matcher {
        @Override
        public boolean doMatch(Object efv, Object qfv) {
            return Boolean.TRUE.equals(qfv) == (efv == null);
        }

        @Override
        public boolean isComparable(Object efv, Object qfv) {
            return true;
        }
    }

}
