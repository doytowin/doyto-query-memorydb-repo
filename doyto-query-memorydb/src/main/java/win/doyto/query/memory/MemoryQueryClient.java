package win.doyto.query.memory;

import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.annotation.View;
import win.doyto.query.core.AggregationQuery;
import win.doyto.query.core.DataQueryClient;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.util.CommonUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;


/**
 * MemoryQueryClient
 *
 * @author f0rb on 2024/7/22
 */
public class MemoryQueryClient implements DataQueryClient {
    @Override
    public <V extends Persistable<I>, I extends Serializable, Q extends DoytoQuery> List<V> query(Q query, Class<V> viewClass) {
        return new ArrayList<>();
    }

    @Override
    public <V extends Persistable<I>, I extends Serializable, Q extends DoytoQuery> long count(Q query, Class<V> viewClass) {
        return 0;
    }

    @Override
    public <V, Q extends DoytoQuery & AggregationQuery> List<V> aggregate(Q query, Class<V> viewClass) {
        Class<?> entityClass = viewClass.getAnnotation(View.class).value();
        List<?> list = DataAccessManager.query(entityClass, query);

        Map<Map<String, Object>, V> groupByMap = list.parallelStream().collect(
                groupingBy(buildGroupByFunc(viewClass), new GroupByCollector<>(viewClass)));
        writeGroupByFields(groupByMap);

        return new ArrayList<>(groupByMap.values());
    }

    private static Function<Object, Map<String, Object>> buildGroupByFunc(Class<?> viewClass) {
        List<String> groupByFields = Arrays.stream(viewClass.getDeclaredFields())
                                           .filter(field -> field.isAnnotationPresent(GroupBy.class))
                                           .map(Field::getName).toList();
        return entity -> groupByFields.stream().collect(toMap(
                k -> k,
                k -> CommonUtil.readField(entity, k)
        ));
    }

    private static <V> void writeGroupByFields(Map<Map<String, Object>, V> groupByMap) {
        for (Map.Entry<Map<String, Object>, V> groupByViewEntry : groupByMap.entrySet()) {
            V view = groupByViewEntry.getValue();
            Set<Map.Entry<String, Object>> entries = groupByViewEntry.getKey().entrySet();
            for (Map.Entry<String, Object> groupByEntry : entries) {
                writeField(view, groupByEntry.getKey(), groupByEntry.getValue());
            }
        }
    }

    @SneakyThrows
    private static void writeField(Object view, String fieldName, Object value) {
        FieldUtils.writeField(view, fieldName, value, true);
    }
}
