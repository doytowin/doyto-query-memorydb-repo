package win.doyto.query.memory.aggregate;

import lombok.Getter;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * ExpressionAggregateMetadata
 *
 * @author f0rb on 2024/7/27
 */
public class ExpressionAggregateMetadata implements AggregateMetadata {
    @Getter
    private final Field field;
    private final ComplexExpressionNode complexExpressionNode;

    public ExpressionAggregateMetadata(Field field, String exp) {
        this.field = field;
        this.complexExpressionNode = Aggregation.build(exp);
    }

    @Override
    public String getLabel() {
        return "*";
    }

    @Override
    public void accumulate(Map<String, List<Object>> map, Object entity) {
        complexExpressionNode.accumulate(map, entity);
    }

    @Override
    public Object execute(Map<String, List<Object>> efvMap) {
        return complexExpressionNode.aggregate(efvMap);
    }
}
