/*
 * Copyright © 2022-2025 DoytoWin, Inc.
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

package win.doyto.query.memory.condition;

import lombok.extern.slf4j.Slf4j;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.QuerySuffix;
import win.doyto.query.memory.MemoryDataAccessManager;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;

import static win.doyto.query.core.QuerySuffix.*;
import static win.doyto.query.util.CommonUtil.getField;
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

    public LeafConditionNode(String queryFieldName, Object queryFieldValue, Class<?> entityClass, String exp) {
        QuerySuffix querySuffix = resolve(queryFieldName);
        this.entityFieldName = querySuffix.resolveColumnName(queryFieldName);

        // do nested query when build leaf node condition
        List<?> list = doSubquery(queryFieldValue, entityClass, exp);
        Object qfv = querySuffix.name().endsWith("In") ? list : list.get(0);
        this.delegate = FilterExecutor.build(querySuffix, qfv);
        this.condition = buildCondition(querySuffix, qfv);
    }

    private static List<?> doSubquery(Object queryFieldValue, Class<?> entityClass, String exp) {
        return MemoryDataAccessManager.aggregate((DoytoQuery) queryFieldValue, entityClass, exp);
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
        Object entityFieldValue = readNestedField(entity, entityFieldName);
        boolean result = delegate.test(entityFieldValue);
        log.debug("Filtering for [{}.{}]: ({} {}) -> {}", entity.getClass().getSimpleName(),
                entityFieldName, entityFieldValue, condition, result);
        return result;
    }

    private static Object readNestedField(Object target, String path) {
        for (String property : path.split("\\.")) {
            Field field = getField(target, property);
            target = readField(field, target);
            if (target == null) {
                return null;
            }
        }
        return target;
    }
}
