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

import lombok.extern.slf4j.Slf4j;
import win.doyto.query.annotation.Column;
import win.doyto.query.core.AggregationPrefix;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.*;
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
@Slf4j
public class GroupByCollector implements Collector<Object, Map<String, List<Object>>, Map<String, Object>> {
    private final List<AggregateMetadata> metadataList;

    public GroupByCollector(Class<?> viewClass) {
        this.metadataList = buildAggregateMetadata(viewClass);
    }

    private static List<AggregateMetadata> buildAggregateMetadata(Class<?> viewClass) {
        List<AggregateMetadata> metadataList = new LinkedList<>();
        for (Field field : viewClass.getDeclaredFields()) {
            Column columnAnno = field.getAnnotation(Column.class);
            if (columnAnno != null) {
                metadataList.add(new ExpressionAggregateMetadata(field, columnAnno.name()));
            } else if (AggregationPrefix.resolveField(field.getName()) != AggregationPrefix.NONE) {
                metadataList.add(new PrefixAggregateMetadata(field));
            }
        }
        return metadataList;
    }

    @Override
    public Supplier<Map<String, List<Object>>> supplier() {
        return () -> {
            Map<String, List<Object>> map = new HashMap<>();
            for (AggregateMetadata am : metadataList) {
                map.put(am.getLabel(), new LinkedList<>());
            }
            return map;
        };
    }

    @Override
    public BiConsumer<Map<String, List<Object>>, Object> accumulator() {
        return (map, entity) -> {
            map.forEach((key, value) -> value.add(CommonUtil.readFieldGetter(entity, key)));
            metadataList.forEach(eam -> eam.accumulate(map, entity));
        };
    }

    @Override
    public BinaryOperator<Map<String, List<Object>>> combiner() {
        return (left, right) -> {
            left.forEach((key, value) -> value.addAll(right.get(key)));
            return left;
        };
    }

    @Override
    public Function<Map<String, List<Object>>, Map<String, Object>> finisher() {
        return map -> {
            Map<String, Object> dataMap = new HashMap<>();
            for (AggregateMetadata aggregateMetadata : metadataList) {
                Object value = aggregateMetadata.execute(map);
                dataMap.put(aggregateMetadata.getField().getName(), value);
            }
            return dataMap;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Stream.of(Characteristics.UNORDERED).collect(Collectors.toSet());
    }

}
