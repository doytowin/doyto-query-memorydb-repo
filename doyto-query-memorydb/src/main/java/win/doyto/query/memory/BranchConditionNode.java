/*
 * Copyright © 2019-2022 Forb Yuan
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
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
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

    private Predicate<Object> predicate;
    private int count;

    /**
     * Construct a branch node of a screening decision tree
     *
     * @param target The object corresponding to the branch node
     */
    public BranchConditionNode(Object target) {
        this(target, true);
    }

    /**
     * Construct a AND/OR branch node of a screening decision tree
     *
     * @param target The object corresponding to the branch node
     * @param and true to an AND node, false to an OR node
     */
    public BranchConditionNode(Object target, boolean and) {
        predicate = and ? t -> true : t -> false;
        for (Field field : target.getClass().getDeclaredFields()) {
            if (ColumnUtil.shouldRetain(field)) {
                Object value = readField(field, target);
                if (isValidValue(value, field)) {
                    if (field.getName().endsWith("Or")) {
                        BranchConditionNode orNode = new BranchConditionNode(value, false);
                        if (orNode.count > 0) {
                            predicate = predicate.and(orNode);
                        }
                    } else {
                        LeafConditionNode leafNode = new LeafConditionNode(field, value);
                        if (and) {
                            predicate = predicate.and(leafNode);
                        } else {
                            predicate = predicate.or(leafNode);
                        }
                        count++;
                    }
                }
            }
        }
    }

    @Override
    public boolean test(Object entity) {
        return predicate.test(entity);
    }
}
