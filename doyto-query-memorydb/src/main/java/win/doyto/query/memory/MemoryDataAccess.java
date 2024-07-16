/*
 * Copyright © 2019-2024 Forb Yuan
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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.annotation.GeneratedValue;
import win.doyto.query.annotation.Id;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.Persistable;
import win.doyto.query.util.BeanUtil;
import win.doyto.query.util.ColumnUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static win.doyto.query.util.CommonUtil.*;

/**
 * MemoryDataAccess
 *
 * @author f0rb
 */
@Slf4j
@SuppressWarnings({"unchecked", "java:S3740"})
public class MemoryDataAccess<E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery> implements DataAccess<E, I, Q> {
    protected static final Map<Class<?>, Map<?, ?>> tableMap = new ConcurrentHashMap<>();

    protected final Map<I, E> entitiesMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final List<Field> fields;
    private final Field idField;
    private final Class<I> idClass;

    public MemoryDataAccess(Class<E> entityClass) {
        tableMap.put(entityClass, entitiesMap);

        // init fields
        fields = ColumnUtil.getColumnFieldsFrom(entityClass);
        Field[] idFields = FieldUtils.getFieldsWithAnnotation(entityClass, Id.class);
        idClass = BeanUtil.getIdClass(entityClass);
        if (idFields.length == 1 && idFields[0].isAnnotationPresent(GeneratedValue.class)) {
            idField = idFields[0];
        } else {
            idField = null;
        }
    }

    protected void generateNewId(E entity) {
        try {
            Object newId = chooseIdValue(idGenerator.incrementAndGet(), idClass);
            writeField(idField, entity, newId);
        } catch (Exception e) {
            log.warn("写入id失败: {} - {}", entity.getClass(), e.getMessage());
        }
    }

    private Object chooseIdValue(Long newId, Class<?> type) {
        Object t = newId;
        if (type.isAssignableFrom(Integer.class)) {
            t = newId.intValue();
        }
        return t;
    }

    @Override
    public E get(IdWrapper<I> idWrapper) {
        return SerializationUtils.clone(entitiesMap.get(idWrapper.getId()));
    }

    @Override
    public List<I> queryIds(Q query) {
        return queryColumns(query, idClass, "id");
    }

    @Override
    public void create(E e) {
        if (idField != null) {
            generateNewId(e);
        }
        entitiesMap.put(e.getId(), e);
    }

    @Override
    public int update(E e) {
        if (!entitiesMap.containsKey(e.getId())) {
            return 0;
        }
        entitiesMap.put(e.getId(), e);
        return 1;
    }

    @Override
    public int patch(E patch) {
        E origin = entitiesMap.get(patch.getId());
        if (origin == null) {
            return 0;
        }

        for (Field field : fields) {
            Object value = readField(field, patch);
            if (value != null) {
                writeField(field, origin, value);
            }
        }
        return 1;
    }

    @Override
    public int patch(E p, Q q) {
        List<E> list = query(q);
        for (E origin : list) {
            p.setId(origin.getId());
            patch(p);
        }
        return list.size();
    }

    @Override
    public int delete(IdWrapper<I> idWrapper) {
        return entitiesMap.remove(idWrapper.getId()) == null ? 0 : 1;
    }

    @Override
    public int delete(Q query) {
        List<E> list = query(query);
        list.stream().map(Persistable::getId).forEach(entitiesMap::remove);
        return list.size();
    }

    @Override
    public List<E> query(Q query) {
        BranchConditionNode root = new BranchConditionNode(query);
        Stream<E> stream = entitiesMap.values().stream().filter(root);

        if (query.getSort() != null) {
            stream = doSort(stream, query.getSort());
        }
        if (query.needPaging()) {
            stream = truncateByPaging(stream, query);
        }
        return stream.map(SerializationUtils::clone).toList();
    }

    private Stream<E> truncateByPaging(Stream<E> stream, Q query) {
        long from = GlobalConfiguration.calcOffset(query);
        return stream.skip(from).limit(query.getPageSize());
    }

    @Override
    public <V> List<V> queryColumns(Q q, Class<V> classV, String... columns) {
        List<E> entities = query(q);
        List<V> objects = new ArrayList<>(entities.size());
        if (columns.length == 1) {
            return entities.stream().map(entity -> (V) readField(entity, columns[0])).toList();
        } else {
            for (E e : entities) {
                objects.add(BeanUtil.convertTo(e, classV));
            }
        }
        return objects;
    }

    protected Stream<E> doSort(Stream<E> stream, String sort) {
        String[] orders = StringUtils.split(sort, ";");
        for (int i = orders.length - 1; i >= 0; i--) {
            String order = orders[i];
            stream = stream.sorted((o1, o2) -> {
                String[] pd = StringUtils.split(order, ",");
                String property = toCamelCase(pd[0]);
                Comparable<Object> c1 = (Comparable<Object>) readField(o1, property);
                Object c2 = readField(o2, property);
                int ret = c1.compareTo(c2);
                return "asc".equalsIgnoreCase(pd[1]) ? ret : -ret;
            });
        }
        return stream;
    }

    @Override
    public long count(Q query) {
        BranchConditionNode root = new BranchConditionNode(query);
        return entitiesMap.values().stream().filter(root).count();
    }

}
