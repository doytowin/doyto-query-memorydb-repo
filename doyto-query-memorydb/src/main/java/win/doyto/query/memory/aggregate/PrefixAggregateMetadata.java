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

package win.doyto.query.memory.aggregate;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.core.AggregationPrefix;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static win.doyto.query.core.AggregationPrefix.resolveField;

/**
 * PrefixAggregateMetadata
 *
 * @author f0rb on 2024/7/23
 */
public class PrefixAggregateMetadata implements AggregateMetadata {
    @Getter
    private final Field field;
    private final String entityFieldName;
    private final ExpressionNode expressionNode;

    public PrefixAggregateMetadata(Field field) {
        this.field = field;
        String name = field.getName();
        AggregationPrefix prefix = resolveField(name);
        String efn = StringUtils.uncapitalize(prefix.resolveColumnName(name));
        this.entityFieldName = efn.equals("*") ? "id" : efn;
        this.expressionNode = new ExpressionNode(prefix.getName(), this.entityFieldName, field.getType());
    }

    @Override
    public String getLabel() {
        return entityFieldName;
    }

    @Override
    public Object execute(Map<String, List<Object>> efvMap) {
        List<Object> efvList = efvMap.get(entityFieldName);
        return this.expressionNode.aggregate(efvList);
    }

}
