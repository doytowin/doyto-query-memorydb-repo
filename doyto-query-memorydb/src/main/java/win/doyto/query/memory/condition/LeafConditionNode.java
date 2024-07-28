package win.doyto.query.memory.condition;

import lombok.extern.slf4j.Slf4j;
import win.doyto.query.core.QuerySuffix;

import java.util.function.Predicate;

import static win.doyto.query.core.QuerySuffix.*;
import static win.doyto.query.util.CommonUtil.readField;

/**
 * LeafConditionNode
 *
 * @author f0rb on 2024/7/16
 */
@Slf4j
public class LeafConditionNode<E> implements ConditionNode<E> {

    private final String entityFieldName;
    private final Predicate<Object> delegate;
    private final String condition;

    public LeafConditionNode(String queryFieldName, Object queryFieldValue) {
        QuerySuffix querySuffix = resolve(queryFieldName);
        this.entityFieldName = querySuffix.resolveColumnName(queryFieldName);
        this.delegate = FilterExecutor.build(querySuffix, queryFieldValue);
        this.condition = buildCondition(querySuffix, queryFieldValue);
    }

    private static String buildCondition(QuerySuffix querySuffix, Object queryFieldValue) {
        if (querySuffix == Null) {
            return Boolean.TRUE.equals(queryFieldValue) ? "== null" : "!= null";
        }
        String op = querySuffix == NONE ? "==" : querySuffix.name();
        return op + " " + queryFieldValue;
    }

    @Override
    public boolean test(E entity) {
        Object entityFieldValue = readField(entity, entityFieldName);
        boolean result = delegate.test(entityFieldValue);
        log.debug("Filtering for [{}.{}]: ({} {}) -> {}", entity.getClass().getSimpleName(),
                entityFieldName, entityFieldValue, condition, result);
        return result;
    }
}
