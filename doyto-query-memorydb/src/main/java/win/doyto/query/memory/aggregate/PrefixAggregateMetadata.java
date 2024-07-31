package win.doyto.query.memory.aggregate;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.core.AggregationPrefix;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static win.doyto.query.core.AggregationPrefix.resolveField;
import static win.doyto.query.memory.aggregate.Aggregation.buildAggrFunc;

/**
 * PrefixAggregateMetadata
 *
 * @author f0rb on 2024/7/23
 */
@Getter
public class PrefixAggregateMetadata implements AggregateMetadata {
    private final Field field;
    private final String entityFieldName;
    private final Function<List<Object>, Object> func;

    public PrefixAggregateMetadata(Field field) {
        this.field = field;
        String name = field.getName();
        AggregationPrefix prefix = resolveField(name);
        this.entityFieldName = StringUtils.uncapitalize(prefix.resolveColumnName(name));
        this.func = buildAggrFunc(prefix, field.getType());
    }

    @Override
    public String getLabel() {
        return entityFieldName;
    }

    @Override
    public Object execute(Map<String, List<Object>> efvMap) {
        List<Object> efvList = efvMap.get(entityFieldName);
        return this.func.apply(efvList);
    }

}
