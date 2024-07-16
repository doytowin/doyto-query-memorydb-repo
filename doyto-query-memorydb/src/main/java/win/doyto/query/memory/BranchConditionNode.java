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

    Predicate<Object> predicate = t -> true;

    /**
     * 根据Query对象为各个字段确定筛选条件
     *
     * @param query Query object
     */
    public BranchConditionNode(Object query) {
        for (Field field : query.getClass().getDeclaredFields()) {
            if (ColumnUtil.shouldRetain(field)) {
                Object value = readField(field, query);
                if (isValidValue(value, field)) {
                    predicate = predicate.and(new LeafConditionNode(field, value));
                }
            }
        }
    }

    @Override
    public boolean test(Object entity) {
        return predicate.test(entity);
    }
}
