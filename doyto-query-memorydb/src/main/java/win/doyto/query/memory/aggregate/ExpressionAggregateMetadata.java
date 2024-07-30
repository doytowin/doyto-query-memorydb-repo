package win.doyto.query.memory.aggregate;

import lombok.Getter;

import java.lang.reflect.Field;
import java.util.List;

import static win.doyto.query.memory.aggregate.GroupByCollector.ORIGIN_ELEM;

/**
 * ExpressionAggregateMetadata
 *
 * @author f0rb on 2024/7/27
 */
public class ExpressionAggregateMetadata implements AggregateMetadata {
    @Getter
    private final Field field;
    private final ExpressionNode expressionNode;

    public ExpressionAggregateMetadata(Field field, String exp) {
        this.field = field;
        this.expressionNode = new ExpressionNode(exp, field.getType());
    }

    @Override
    public String getLabel() {
        return ORIGIN_ELEM;
    }

    @Override
    public Object execute(List<Object> entityList) {
        List<Object> results = entityList.stream().map(expressionNode::compute).toList();
        return expressionNode.summarize(results);
    }
}
