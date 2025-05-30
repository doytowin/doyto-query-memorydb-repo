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

package win.doyto.query.memory;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.annotation.View;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.memory.aggregate.Aggregation;
import win.doyto.query.memory.aggregate.GroupByCollector;
import win.doyto.query.memory.aggregate.SingleColumnGroupByCollector;
import win.doyto.query.memory.condition.BranchConditionNode;
import win.doyto.query.memory.datamapper.DataMapper;
import win.doyto.query.memory.datamapper.DefaultDataMapper;
import win.doyto.query.memory.datawrapper.FileDataWrapper;
import win.doyto.query.memory.datawrapper.FileIOException;
import win.doyto.query.memory.datawrapper.FileType;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static win.doyto.query.util.CommonUtil.readField;
import static win.doyto.query.util.CommonUtil.toCamelCase;

/**
 * MemoryDataAccessManager
 *
 * @author f0rb on 2024/7/23
 */
@UtilityClass
public class MemoryDataAccessManager {
    final Map<Class<?>, MemoryDataAccess<?, ?, ? super DoytoQuery>> dataAccessMap = new HashMap<>();

    public synchronized <E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    MemoryDataAccess<E, I, Q> create(Class<E> entityClass) {
        return create(entityClass, null);
    }

    public synchronized <E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    MemoryDataAccess<E, I, Q> create(Class<E> entityClass, String store) {
        return create(entityClass, store, FileType.BSON);
    }

    @SuppressWarnings({"unchecked"})
    public synchronized <E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    MemoryDataAccess<E, I, Q> create(Class<E> entityClass, String store, FileType fileType) {
        if (dataAccessMap.containsKey(entityClass)) {
            return (MemoryDataAccess<E, I, Q>) (MemoryDataAccess<E, I, DoytoQuery>) dataAccessMap.get(entityClass);
        }
        MemoryDataAccess<E, I, DoytoQuery> dataAccess = new MemoryDataAccess<>(entityClass);
        if (store != null) {
            String dataRoot = store + entityClass.getSimpleName();
            dataAccess.setCreateDataWrapperFunc(e -> new FileDataWrapper<>(e, fileType, dataRoot).flush());
            File root = new File(dataRoot);
            if (!root.exists() && !root.mkdirs()) {
                throw new FileIOException("Failed to create data directory: " + dataRoot);
            }
            dataAccess.loadData(entityClass, root, fileType);
        }

        dataAccessMap.put(entityClass, dataAccess);
        return (MemoryDataAccess<E, I, Q>) dataAccess;
    }

    @SuppressWarnings({"unchecked", "java:S3740"})
    public static <E> List<E> query(Class<E> entityClass, DoytoQuery query, Predicate another) {
        MemoryDataAccess<?, ?, ? super DoytoQuery> dataAccess = dataAccessMap.get(entityClass);
        return dataAccess.filter(query, another).toList();
    }

    @SuppressWarnings({"unchecked"})
    public static <E> List<E> query(Class<E> entityClass, DoytoQuery query) {
        MemoryDataAccess<?, ?, ? super DoytoQuery> dataAccess = dataAccessMap.get(entityClass);
        return (List<E>) dataAccess.filter(query).toList();
    }

    static LinkedHashMap<String, Integer> buildSortingMap(String sort) {
        LinkedHashMap<String, Integer> sortMap = new LinkedHashMap<>();
        if (StringUtils.isBlank(sort)) return sortMap;
        String[] orders = StringUtils.split(sort, ";");
        for (int i = orders.length - 1; i >= 0; i--) {
            String order = orders[i];
            String[] pd = StringUtils.split(order, ",");
            String property = toCamelCase(pd[0]);
            String ascending = pd.length == 2 ? pd[1] : "asc";
            sortMap.put(property, "asc".equals(ascending) ? 1 : -1);
        }
        return sortMap;
    }

    static <V> Stream<V> sorting(Stream<V> stream, LinkedHashMap<String, Integer> sortingMap) {
        for (Map.Entry<String, Integer> entry : sortingMap.entrySet()) {
            stream = stream.sorted((o1, o2) -> {
                Object c1 = readField(o1, entry.getKey());
                Object c2 = readField(o2, entry.getKey());
                int ret = Aggregation.compare(c1, c2);
                return entry.getValue() > 0 ? ret : -ret;
            });
        }
        return stream;
    }

    public <V> List<V> aggregate(Class<V> viewClass, DoytoQuery query) {
        return aggregate(viewClass, query, new DefaultDataMapper<>(viewClass));
    }

    public <V> List<V> aggregate(Class<V> viewClass, DoytoQuery query, DataMapper<V> dataMapper) {
        Class<?> entityClass = viewClass.getAnnotation(View.class).value();
        List<?> list = query(entityClass, query);

        Map<Map<String, Object>, Map<String, Object>> groupByMap = list.parallelStream().collect(
                groupingBy(buildGroupByFunc(viewClass), new GroupByCollector(viewClass)));
        groupByMap.forEach((groupMap, dataMap) -> dataMap.putAll(groupMap));

        BranchConditionNode<V> root = BranchConditionNode.buildHaving(query);
        Stream<V> stream = groupByMap.values().stream().map(dataMapper::map).filter(root);

        LinkedHashMap<String, Integer> sortingMap = buildSortingMap(query.getSort());
        return sorting(stream, sortingMap).toList();
    }

    public List<Object> aggregate(DoytoQuery query, Class<?> entityClass, String exp) {
        List<?> list = query(entityClass, query);

        Map<Map<String, Object>, Object> groupByMap = list.parallelStream().collect(
                groupingBy(buildGroupByFunc(entityClass), new SingleColumnGroupByCollector(exp)));

        return new ArrayList<>(groupByMap.values());
    }

    private Function<Object, Map<String, Object>> buildGroupByFunc(Class<?> viewClass) {
        List<String> groupByFields = Arrays.stream(viewClass.getDeclaredFields())
                                           .filter(field -> field.isAnnotationPresent(GroupBy.class))
                                           .map(Field::getName).toList();
        return entity -> groupByFields.stream().collect(toMap(
                k -> k,
                k -> readField(entity, k)
        ));
    }

}
