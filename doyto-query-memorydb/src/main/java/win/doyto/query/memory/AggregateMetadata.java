package win.doyto.query.memory;

import lombok.Getter;
import win.doyto.query.core.AggregationPrefix;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        AggregationPrefix prefix = AggregationPrefix.resolveField(name);
        this.entityFieldName = prefix.resolveColumnName(name);
        this.func = efvList -> efvList.stream().collect(Collectors.averagingDouble(value -> ((Number) value).floatValue()));
    }

    public Object execute(List<Object> efvList) {
        return this.func.apply(efvList);
    }
}
