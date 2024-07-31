package win.doyto.query.memory.aggregate;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import win.doyto.query.annotation.Column;
import win.doyto.query.core.AggregationPrefix;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
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
@Slf4j
public class GroupByCollector<V> implements Collector<Object, Map<String, List<Object>>, V> {
    private final Class<V> viewClass;
    private final List<AggregateMetadata> metadataList = new LinkedList<>();

    public GroupByCollector(Class<V> viewClass) {
        this.viewClass = viewClass;
        for (Field field : viewClass.getDeclaredFields()) {
            Column columnAnno = field.getAnnotation(Column.class);
            if (columnAnno != null) {
                metadataList.add(new ExpressionAggregateMetadata(field, columnAnno.name()));
            } else if (AggregationPrefix.resolveField(field.getName()) != AggregationPrefix.NONE) {
                metadataList.add(new PrefixAggregateMetadata(field));
            }
        }
    }

    @Override
    public Supplier<Map<String, List<Object>>> supplier() {
        return () -> {
            Map<String, List<Object>> map = new HashMap<>();
            for (AggregateMetadata am : metadataList) {
                map.put(am.getLabel(), new LinkedList<>());
            }
            return map;
        };
    }

    @Override
    public BiConsumer<Map<String, List<Object>>, Object> accumulator() {
        return (map, entity) -> {
            map.forEach((key, value) -> value.add(CommonUtil.readFieldGetter(entity, key)));
            metadataList.forEach(eam -> eam.accumulate(map, entity));
        };
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
                Object value = aggregateMetadata.execute(map);
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
