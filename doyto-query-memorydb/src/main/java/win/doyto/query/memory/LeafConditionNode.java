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
