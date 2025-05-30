/*
 * Copyright © 2022-2025 DoytoWin, Inc.
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

package win.doyto.query.memory.aggregate;

import lombok.experimental.UtilityClass;
import win.doyto.query.core.AggregationPrefix;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Aggregation
 *
 * @author f0rb on 2024/7/31
 */
@UtilityClass
public class Aggregation {
    private static final Pattern PREFIX_PTN = Pattern.compile(
            Arrays.stream(AggregationPrefix.values())
                  .map(Enum::name)
                  .collect(Collectors.joining("|", "(", ")\\(([^\\)]+)\\)")));
    protected static final AtomicInteger COUNT = new AtomicInteger();

    public static ComplexExpressionNode build(String exp) {
        Matcher matcher = PREFIX_PTN.matcher(exp);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Illegal expression provided!");
        }

        StringBuilder newExpBuilder = new StringBuilder();
        Map<String, ExpressionNode> expNodeMap = new HashMap<>();
        do {
            String alias = "sub" + COUNT.getAndIncrement();
            matcher.appendReplacement(newExpBuilder, alias);
            ExpressionNode expNode = new ExpressionNode(matcher.group(1), matcher.group(2), double.class);
            expNodeMap.put(alias, expNode);
        } while (matcher.find());

        String newExp = matcher.appendTail(newExpBuilder).toString();
        return new ComplexExpressionNode(newExp, expNodeMap);
    }

    static Function<List<Object>, Object> buildAggrFunc(AggregationPrefix prefix, Class<?> type) {
        return switch (prefix) {
            case max -> efvList -> efvList.stream().max(Aggregation::compare).orElse(0);
            case min -> efvList -> efvList.stream().min(Aggregation::compare).orElse(0);
            case first -> efvList -> efvList.stream().findFirst().orElse(null);
            case last -> efvList -> efvList.isEmpty() ? null : efvList.get(efvList.size() - 1);
            case sum -> efvList -> {
                double sum = efvList.stream().mapToDouble(value -> ((Number) value).doubleValue()).sum();
                if (Integer.class.isAssignableFrom(type)) {
                    return (int) sum;
                }
                return sum;
            };
            case avg ->
                    efvList -> efvList.stream().collect(Collectors.averagingDouble(value -> ((Number) value).doubleValue()));
            default -> List::size;
        };
    }

    @SuppressWarnings("unchecked")
    public static int compare(Object efv, Object qfv) {
        if (efv instanceof Number n1 && qfv instanceof Number n2) {
            return Double.compare(n1.doubleValue(), n2.doubleValue());
        }
        return ((Comparable<Object>) efv).compareTo(qfv);
    }
}
