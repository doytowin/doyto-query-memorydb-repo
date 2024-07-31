package win.doyto.query.memory.aggregate;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.core.AggregationPrefix;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static win.doyto.query.core.AggregationPrefix.resolveField;

/**
 * PrefixAggregateMetadata
 *
 * @author f0rb on 2024/7/23
 */
public class PrefixAggregateMetadata implements AggregateMetadata {
    @Getter
    private final Field field;
    private final String entityFieldName;
    private final ExpressionNode expressionNode;

    public PrefixAggregateMetadata(Field field) {
        this.field = field;
        String name = field.getName();
        AggregationPrefix prefix = resolveField(name);
        String efn = StringUtils.uncapitalize(prefix.resolveColumnName(name));
        this.entityFieldName = efn.equals("*") ? "id" : efn;
        this.expressionNode = new ExpressionNode(prefix.getName(), this.entityFieldName, field.getType());
    }

    @Override
    public String getLabel() {
        return entityFieldName;
    }

    @Override
    public Object execute(Map<String, List<Object>> efvMap) {
        List<Object> efvList = efvMap.get(entityFieldName);
        return this.expressionNode.aggregate(efvList);
    }

}
