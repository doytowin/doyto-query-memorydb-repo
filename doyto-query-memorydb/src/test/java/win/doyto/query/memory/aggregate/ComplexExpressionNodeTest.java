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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * ComplexExpressionNodeTest
 *
 * @author f0rb on 2024/7/30
 */
class ComplexExpressionNodeTest {

    @Test
    void buildNode() {
        int i = Aggregation.COUNT.get();
        ComplexExpressionNode expressionNode = Aggregation.build("avg(salary + bonus) / salary");
        assertThat(expressionNode.expression.getVariableNames()).containsExactly("sub" + i, "salary");
    }

    @Test
    void supportTwoAggrFunc() {
        int i = Aggregation.COUNT.get();
        ComplexExpressionNode expressionNode = Aggregation.build("avg(salary + bonus) / sum(salary)");
        assertThat(expressionNode.expression.getVariableNames()).containsExactly("sub" + i++, "sub" + i);
    }

}