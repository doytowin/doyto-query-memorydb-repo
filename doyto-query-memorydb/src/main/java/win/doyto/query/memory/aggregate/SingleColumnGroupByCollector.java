package win.doyto.query.memory.aggregate;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * GroupByCollector
 *
 * @author f0rb on 2024/7/22
 */
public class SingleColumnGroupByCollector implements Collector<Object, List<Object>, Object> {

    private final ExpressionNode node;

    public SingleColumnGroupByCollector(String exp) {
        this.node = new ExpressionNode(exp, Object.class);
    }

    @Override
    public Supplier<List<Object>> supplier() {
        return LinkedList::new;
    }

    @Override
    public BiConsumer<List<Object>, Object> accumulator() {
        return (container, entity) -> container.add(node.compute(entity));
    }

    @Override
    public BinaryOperator<List<Object>> combiner() {
        return (left, right) -> {
            left.addAll(right);
            return left;
        };
    }

    @Override
    public Function<List<Object>, Object> finisher() {
        return node::aggregate;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Stream.of(Characteristics.UNORDERED).collect(Collectors.toSet());
    }

}
