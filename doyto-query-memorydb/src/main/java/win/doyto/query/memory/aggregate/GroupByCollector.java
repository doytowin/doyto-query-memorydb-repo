package win.doyto.query.memory.aggregate;

import lombok.SneakyThrows;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.util.CommonUtil;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * GroupByCollector
 *
 * @author f0rb on 2024/7/22
 */
public class GroupByCollector<V> implements Collector<Object, Map<String, List<Object>>, V> {

    private final Class<V> viewClass;
    private final List<AggregateMetadata> metadataList;

    public GroupByCollector(Class<V> viewClass) {
        this.viewClass = viewClass;
        this.metadataList = Arrays
                .stream(viewClass.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(GroupBy.class))
                .map(AggregateMetadata::new).toList();
    }

    @Override
    public Supplier<Map<String, List<Object>>> supplier() {
        return () -> {
            Map<String, List<Object>> map = new HashMap<>();
            for (AggregateMetadata k : metadataList) {
                map.put(k.getEntityFieldName(), new LinkedList<>());
            }
            return map;
        };
    }

    @Override
    public BiConsumer<Map<String, List<Object>>, Object> accumulator() {
        return (map, entity) -> map.forEach((key, value) -> value.add(CommonUtil.readFieldGetter(entity, key)));
    }

    @Override
    public BinaryOperator<Map<String, List<Object>>> combiner() {
        return (left, right) -> {
            left.forEach((key, value) -> value.addAll(right.get(key)));
            return left;
        };
    }

    @SneakyThrows
    private V createTarget() {
        return viewClass.getConstructor().newInstance();
    }

    @Override
    public Function<Map<String, List<Object>>, V> finisher() {
        return map -> {
            V view = createTarget();
            for (AggregateMetadata aggregateMetadata : metadataList) {
                List<Object> efvList = map.get(aggregateMetadata.getEntityFieldName());
                Object value = aggregateMetadata.execute(efvList);
                CommonUtil.writeField(aggregateMetadata.getField(), view, value);
            }
            return view;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Stream.of(Characteristics.UNORDERED).collect(Collectors.toSet());
    }

}
