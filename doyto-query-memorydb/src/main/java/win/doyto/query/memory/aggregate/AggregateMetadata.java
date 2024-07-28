package win.doyto.query.memory.aggregate;

import lombok.Getter;
import win.doyto.query.core.AggregationPrefix;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static win.doyto.query.core.AggregationPrefix.resolveField;

/**
 * AggregateMetadata
 *
 * @author f0rb on 2024/7/23
 */
@Getter
public class AggregateMetadata {
    private final Field field;
    private final String entityFieldName;
    private final Function<List<Object>, Object> func;

    public AggregateMetadata(Field field) {
        this.field = field;
        String name = field.getName();
        AggregationPrefix prefix = resolveField(name);
        this.entityFieldName = prefix.resolveColumnName(name);
        this.func = buildAggrFunc(prefix, field.getType());
    }

    private static Function<List<Object>, Object> buildAggrFunc(AggregationPrefix prefix, Class<?> type) {
        return switch (prefix) {
            case max -> efvList -> efvList.stream().max(AggregateMetadata::compare).orElse(0);
            case min -> efvList -> efvList.stream().min(AggregateMetadata::compare).orElse(0);
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

    public Object execute(List<Object> efvList) {
        return this.func.apply(efvList);
    }
}
