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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class SingleColumnGroupByCollector implements Collector<Object, Map<String, List<Object>>, Object> {

    private final ComplexExpressionNode node;

    public SingleColumnGroupByCollector(String exp) {
        this.node = Aggregation.build(exp);
    }

    @Override
    public Supplier<Map<String, List<Object>>> supplier() {
        return HashMap::new;
    }

    @Override
    public BiConsumer<Map<String, List<Object>>, Object> accumulator() {
        return node::accumulate;
    }

    @Override
    public BinaryOperator<Map<String, List<Object>>> combiner() {
        return (left, right) -> {
            left.forEach((key, value) -> value.addAll(right.get(key)));
            return left;
        };
    }

    @Override
    public Function<Map<String, List<Object>>, Object> finisher() {
        return node::aggregate;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Stream.of(Characteristics.UNORDERED).collect(Collectors.toSet());
    }

}
