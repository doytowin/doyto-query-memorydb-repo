package win.doyto.query.memory.aggregate;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import win.doyto.query.core.AggregationPrefix;
import win.doyto.query.util.CommonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static win.doyto.query.memory.aggregate.Aggregation.buildAggrFunc;

/**
 * ExpressionNode
 *
 * @author f0rb on 2024/7/28
 */
public class ExpressionNode {

    private final Expression expression;
    private final Function<List<Object>, Object> aggrFunc;

    public ExpressionNode(String funcName, String exp, Class<?> returnType) {
        AggregationPrefix prefix = AggregationPrefix.valueOf(funcName);
        this.aggrFunc = buildAggrFunc(prefix, returnType);
        this.expression = AviatorEvaluator.compile(exp);
    }

    public Object compute(Object entity) {
        List<String> columns = expression.getVariableNames();
        Map<String, Object> env = new HashMap<>();
        for (String column : columns) {
            env.put(column, CommonUtil.readFieldGetter(entity, column));
        }
        return expression.execute(env);
    }

    public Object aggregate(List<Object> results) {
        return aggrFunc.apply(results);
    }
}
