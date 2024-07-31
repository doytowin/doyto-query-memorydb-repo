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
    private static final AtomicInteger COUNT = new AtomicInteger();

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
            ExpressionNode expNode = new ExpressionNode(matcher.group(), double.class);
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
                if (type.isAssignableFrom(Integer.class)) {
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
    private static int compare(Object o1, Object o2) {
        return ((Comparable<Object>) o1).compareTo(o2);
    }
}
