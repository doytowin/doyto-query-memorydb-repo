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
        ComplexExpressionNode expressionNode = Aggregation.build("avg(salary + bonus) / salary");
        assertThat(expressionNode.expression.getVariableNames()).containsExactly("sub0", "salary");
    }

    @Test
    void supportTwoAggrFunc() {
        ComplexExpressionNode expressionNode = Aggregation.build("avg(salary + bonus) / sum(salary)");
        assertThat(expressionNode.expression.getVariableNames()).containsExactly("sub1", "sub2");
    }

}