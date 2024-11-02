package win.doyto.query.memory.condition;

import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.Subquery;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.Having;
import win.doyto.query.core.Query;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
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
    private static final BranchConditionNode<?> EMPTY_NODE = new BranchConditionNode<>(t -> true, 0);

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
        this(target, true, EMPTY);
    }

    /**
     * Construct a AND/OR branch node of a filter tree
     *
     * @param target The object corresponding to the branch node
     * @param and    true to an AND node, false to an OR node
     * @param path   as alias for nested query
     */
    public BranchConditionNode(Object target, boolean and, String path) {
        this(target, and, path, queryFields(target.getClass()));
    }

    BranchConditionNode(Object target, boolean and, String path, Field[] fields) {
        delegate = and ? t -> true : t -> false;
        for (Field field : fields) {
            Object value = readField(field, target);
            if (isValidValue(value, field)) {
                ConditionNode<E> child = buildChild(field, value, path);
                if (!(child instanceof BranchConditionNode<?> branchNode) || branchNode.count > 0) {
                    // add when child is leaf node or non-empty branch node
                    delegate = and ? delegate.and(child) : delegate.or(child);
                    count++;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> BranchConditionNode<T> emptyNode() {
        return (BranchConditionNode<T>) EMPTY_NODE;
    }

    public static <V> BranchConditionNode<V> buildHaving(DoytoQuery query) {
        if (query instanceof Having) {
            Field[] fields = Arrays.stream(query.getClass().getDeclaredFields())
                                   .filter(ColumnUtil::shouldRetain).toArray(Field[]::new);
            return new BranchConditionNode<>(query, true, EMPTY, fields);
        }
        return emptyNode();
    }

    public static Field[] queryFields(Class<?> queryClass) {
        if (Arrays.stream(queryClass.getInterfaces()).toList().contains(Having.class)) {
            queryClass = queryClass.getSuperclass();
        }
        return classFieldsMap.computeIfAbsent(queryClass, BranchConditionNode::filterFields);
    }

    private static Field[] filterFields(Class<?> queryClass) {
        return ColumnUtil.filterFields(queryClass, ColumnUtil::shouldRetain).toArray(Field[]::new);
    }

    private static <T> ConditionNode<T> buildChild(Field queryField, Object queryFieldValue, String path) {
        String alias = StringUtils.isBlank(path) ? EMPTY : path + ".";
        ConditionNode<T> child;
        if (queryField.getName().endsWith("Or")) {
            child = buildOrBranchNode(queryField, queryFieldValue);
        } else {
            if (Query.class.isAssignableFrom(queryField.getType())) {
                child = new BranchConditionNode<>(queryFieldValue, true, alias + queryField.getName());
            } else if (DoytoQuery.class.isAssignableFrom(queryField.getType())) {
                if (queryField.isAnnotationPresent(Subquery.class)) {
                    Subquery subquery = queryField.getAnnotation(Subquery.class);
                    child = new LeafConditionNode<>(queryField.getName(), queryFieldValue, subquery.from()[0], subquery.select());
                } else if (queryField.getName().endsWith("And")) {
                    child = new BranchConditionNode<>(queryFieldValue);
                } else {
                    child = emptyNode();
                }
            } else {
                child = new LeafConditionNode<>(alias + queryField.getName(), queryFieldValue);
            }
        }
        return child;
    }

    private static <T> ConditionNode<T> buildOrBranchNode(Field queryField, Object qfv) {
        ConditionNode<T> child;
        if (Collection.class.isAssignableFrom(queryField.getType()) && qfv instanceof Collection<?> list) {
            Class<?> clazz = CommonUtil.resolveActualReturnClass(queryField);
            if (Query.class.isAssignableFrom(clazz)) {
                child = buildOrBranchNodeForListWithCustomType(list);
            } else {
                String queryFieldName = StringUtils.remove(queryField.getName(), "Or");
                child = buildOrBranchNodeForListWithBasicType(list, queryFieldName);
            }
        } else {
            child = new BranchConditionNode<>(qfv, false, EMPTY);
        }
        return child;
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
