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

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * ComplexExpressionNode
 *
 * @author f0rb on 2024/7/30
 */
public class ComplexExpressionNode {

    protected Expression expression;
    private final Map<String, ExpressionNode> subNodeMap;

    public ComplexExpressionNode(String exp, Map<String, ExpressionNode> subNodeMap) {
        this.expression = AviatorEvaluator.compile(exp);
        this.subNodeMap = subNodeMap;
    }

    public void accumulate(Map<String, List<Object>> map, Object entity) {
        // compute each sub expression and store the result
        subNodeMap.forEach((key, value) -> {
            Object object = value.compute(entity);
            map.computeIfAbsent(key, t -> new LinkedList<>()).add(object);
        });
    }

    public Object aggregate(Map<String, List<Object>> efvMap) {
        // aggregate each sub expression and store the result
        Map<String, Object> env = new HashMap<>();
        subNodeMap.forEach((key, expressionNode) -> {
            List<Object> results = efvMap.get(key);
            Object summarize = expressionNode.aggregate(results);
            env.put(key, summarize);
        });
        return expression.execute(env);
    }

}
