package win.doyto.query.memory.condition;

import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.Subquery;
import win.doyto.query.core.Query;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static win.doyto.query.core.QuerySuffix.isValidValue;
import static win.doyto.query.util.CommonUtil.readField;

/**
 * BranchConditionNode
 *
 * @author f0rb on 2024/7/16
 */
public class BranchConditionNode<E> implements ConditionNode<E> {
    private static final Map<Class<?>, Field[]> classFieldsMap = new ConcurrentHashMap<>();

    private Predicate<E> delegate;
    private int count;

    BranchConditionNode(Predicate<E> predicate, int count) {
        this.delegate = predicate;
        this.count = count;
    }

    /**
     * Construct a branch node of a filter tree
     *
     * @param target The object corresponding to the branch node
     */
    public BranchConditionNode(Object target) {
        this(target, true);
    }

    /**
     * Construct a AND/OR branch node of a filter tree
     *
     * @param target The object corresponding to the branch node
     * @param and    true to an AND node, false to an OR node
     */
    public BranchConditionNode(Object target, boolean and) {
        delegate = and ? t -> true : t -> false;
        Field[] fields = queryFields(target.getClass());
        for (Field field : fields) {
            Object value = readField(field, target);
            if (isValidValue(value, field)) {
                ConditionNode<E> child = buildChild(field, value);
                if (!(child instanceof BranchConditionNode<?> branchNode) || branchNode.count > 0) {
                    // add when child is leaf node or non-empty branch node
                    delegate = and ? delegate.and(child) : delegate.or(child);
                    count++;
                }
            }
        }
    }

    public static Field[] queryFields(Class<?> queryClass) {
        return classFieldsMap.computeIfAbsent(queryClass, BranchConditionNode::filterFields);
    }

    private static Field[] filterFields(Class<?> queryClass) {
        return ColumnUtil.filterFields(queryClass, ColumnUtil::shouldRetain).toArray(Field[]::new);
    }

    private static <T> ConditionNode<T> buildChild(Field queryField, Object queryFieldValue) {
        return buildChild(queryField, queryFieldValue, EMPTY);
    }

    private static <T> ConditionNode<T> buildChild(Field queryField, Object queryFieldValue, String path) {
        String alias = StringUtils.isBlank(path) ? EMPTY : path + ".";
        ConditionNode<T> child;
        if (queryField.getName().endsWith("Or")) {
            if (Collection.class.isAssignableFrom(queryField.getType())
                    && queryFieldValue instanceof Collection<?> list) {
                Class<?> clazz = CommonUtil.resolveActualReturnClass(queryField);
                if (Query.class.isAssignableFrom(clazz)) {
                    child = buildOrBranchNodeForListWithCustomType(list);
                } else {
                    String queryFieldName = StringUtils.remove(queryField.getName(), "Or");
                    child = buildOrBranchNodeForListWithBasicType(list, queryFieldName);
                }
            } else {
                child = new BranchConditionNode<>(queryFieldValue, false);
            }
        } else {
            Subquery subquery = queryField.getAnnotation(Subquery.class);
            if (subquery != null) {
                child = new LeafConditionNode<>(queryField.getName(),
                        queryFieldValue, subquery.from()[0], subquery.select());
            } else if (Query.class.isAssignableFrom(queryField.getType())) {
                child = buildAndBranchNodeForNested(alias + queryField.getName(), queryFieldValue);
            } else {
                child = new LeafConditionNode<>(alias + queryField.getName(), queryFieldValue);
            }
        }
        return child;
    }

    private static <E> ConditionNode<E> buildAndBranchNodeForNested(String path, Object target) {
        Predicate<E> branch = t -> true;
        int count = 0;
        Field[] fields = ColumnUtil.queryFields(target.getClass());
        for (Field field : fields) {
            Object value = readField(field, target);
            if (isValidValue(value, field)) {
                ConditionNode<E> child = buildChild(field, value, path);
                if (!(child instanceof BranchConditionNode<?> branchNode) || branchNode.count > 0) {
                    // add when child is leaf node or non-empty branch node
                    branch = branch.and(child);
                    count++;
                }
            }
        }
        return new BranchConditionNode<>(branch, count);
    }

    private static <T> ConditionNode<T> buildOrBranchNodeForListWithCustomType(Collection<?> list) {
        Predicate<T> branch = t -> false;
        for (Object qfv : list) {
            branch = branch.or(new BranchConditionNode<>(qfv));
        }
        return new BranchConditionNode<>(branch, list.size());
    }

    private static <T> BranchConditionNode<T> buildOrBranchNodeForListWithBasicType(Collection<?> list, String queryFieldName) {
        Predicate<T> leaf = t -> false;
        for (Object qfv : list) {
            leaf = leaf.or(new LeafConditionNode<>(queryFieldName, qfv));
        }
        return new BranchConditionNode<>(leaf, list.size());
    }

    @Override
    public boolean test(E entity) {
        return delegate.test(entity);
    }
}
