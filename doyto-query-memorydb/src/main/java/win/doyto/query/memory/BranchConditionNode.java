/*
 * Copyright © 2019-2024 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.memory;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.function.Predicate;

import static win.doyto.query.core.QuerySuffix.isValidValue;
import static win.doyto.query.util.CommonUtil.readField;

/**
 * BranchConditionNode
 *
 * @author f0rb on 2024/7/16
 */
@Getter
public class BranchConditionNode implements ConditionNode {

    private Predicate<Object> delegate;
    private int count;

    BranchConditionNode(Predicate<Object> predicate, int count) {
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
        Field[] fields = ColumnUtil.queryFields(target.getClass());
        for (Field field : fields) {
            Object value = readField(field, target);
            if (isValidValue(value, field)) {
                ConditionNode child = buildChild(field, value);
                if (!(child instanceof BranchConditionNode branchNode) || branchNode.count > 0) {
                    // add when child is leaf node or non-empty branch node
                    delegate = and ? delegate.and(child) : delegate.or(child);
                    count++;
                }
            }
        }
    }

    private static ConditionNode buildChild(Field queryField, Object queryFieldValue) {
        ConditionNode child;
        if (queryField.getName().endsWith("Or")) {
            if (Collection.class.isAssignableFrom(queryField.getType())
                    && queryFieldValue instanceof Collection<?> list) {
                String queryFieldName = StringUtils.remove(queryField.getName(), "Or");
                child = buildBranchNodeForListWithBasicType(list, queryFieldName);
            } else {
                child = new BranchConditionNode(queryFieldValue, false);
            }
        } else {
            child = new LeafConditionNode(queryField.getName(), queryFieldValue);
        }
        return child;
    }

    private static BranchConditionNode buildBranchNodeForListWithBasicType(Collection<?> list, String queryFieldName) {
        Predicate<Object> leaf = t -> false;
        for (Object qfv : list) {
            leaf = leaf.or(new LeafConditionNode(queryFieldName, qfv));
        }
        return new BranchConditionNode(leaf, list.size());
    }

    @Override
    public boolean test(Object entity) {
        return delegate.test(entity);
    }
}
