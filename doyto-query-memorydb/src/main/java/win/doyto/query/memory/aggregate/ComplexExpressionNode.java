package win.doyto.query.memory.aggregate;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * ComplexExpressionNode
 *
 * @author f0rb on 2024/7/30
 */
public class ComplexExpressionNode {

    protected Expression expression;
    private final Map<String, ExpressionNode> subNodeMap;

    public ComplexExpressionNode(String exp, Map<String, ExpressionNode> subNodeMap) {
        this.expression = AviatorEvaluator.compile(exp);
        this.subNodeMap = subNodeMap;
    }

    public void accumulate(Map<String, List<Object>> map, Object entity) {
        // compute each sub expression and store the result
        subNodeMap.forEach((key, value) -> {
            Object object = value.compute(entity);
            map.computeIfAbsent(key, t -> new LinkedList<>()).add(object);
        });
    }

    public Object aggregate(Map<String, List<Object>> efvMap) {
        // aggregate each sub expression and store the result
        Map<String, Object> env = new HashMap<>();
        subNodeMap.forEach((key, expressionNode) -> {
            List<Object> results = efvMap.get(key);
            Object summarize = expressionNode.aggregate(results);
            env.put(key, summarize);
        });
        return expression.execute(env);
    }

}
