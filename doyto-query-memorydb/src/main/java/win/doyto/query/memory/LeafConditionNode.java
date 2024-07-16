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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import win.doyto.query.core.QuerySuffix;

import java.util.function.Predicate;

import static win.doyto.query.core.QuerySuffix.resolve;
import static win.doyto.query.util.CommonUtil.readField;

/**
 * LeafConditionNode
 *
 * @author f0rb on 2024/7/16
 */
@Slf4j
public class LeafConditionNode implements ConditionNode {

    private final String fieldName;
    private final Predicate<Object> predicate;
    private final QuerySuffix querySuffix;
    private final Object rightValue;

    public LeafConditionNode(String queryFieldName, Object rv) {
        this.querySuffix = resolve(queryFieldName);
        this.fieldName = querySuffix.resolveColumnName(queryFieldName);
        this.rightValue = rv;
        Matcher matcher = FilterExecutor.get(querySuffix);
        this.predicate = entityFieldValue -> matcher.match(this.rightValue, entityFieldValue);
    }

    @Override
    public boolean test(Object entity) {
        Object entityFieldValue = readField(entity, fieldName);
        boolean result = predicate.test(entityFieldValue);
        log.debug("Field name [{}], predicate [{} {} {}]: {}", fieldName,  entityFieldValue,querySuffix, rightValue, result);
        return result;
    }
}
