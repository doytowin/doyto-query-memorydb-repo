package win.doyto.query.memory.aggregate;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import win.doyto.query.core.AggregationPrefix;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static win.doyto.query.memory.aggregate.PrefixAggregateMetadata.buildAggrFunc;

/**
 * ExpressionNode
 *
 * @author f0rb on 2024/7/28
 */
public class ExpressionNode {

    private static final Pattern PREFIX_PTN = Pattern.compile(
            Arrays.stream(AggregationPrefix.values())
                  .map(Enum::name)
                  .collect(Collectors.joining("|", "^\\b(", ")\\((.+)\\)")));

    private final Expression expression;
    private final Function<List<Object>, Object> func;

    public ExpressionNode(Field field, String exp) {
        Matcher matcher = PREFIX_PTN.matcher(exp);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Illegal expression provided!");
        }
        AggregationPrefix prefix = AggregationPrefix.valueOf(matcher.group(1));
        this.func = buildAggrFunc(prefix, field.getType());
        this.expression = AviatorEvaluator.compile(matcher.group(2));
    }

    Object compute(Object entity) {
        List<String> columns = expression.getVariableNames();
        Map<String, Object> env = new HashMap<>();
        for (String column : columns) {
            env.put(column, CommonUtil.readFieldGetter(entity, column));
        }
        return expression.execute(env);
    }

    public Object summarize(List<Object> results) {
        return func.apply(results);
    }
}
