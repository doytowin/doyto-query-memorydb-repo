package win.doyto.query.memory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.annotation.GeneratedValue;
import win.doyto.query.annotation.Id;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.*;
import win.doyto.query.entity.Persistable;
import win.doyto.query.memory.condition.BranchConditionNode;
import win.doyto.query.memory.datawrapper.*;
import win.doyto.query.util.BeanUtil;
import win.doyto.query.util.ColumnUtil;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;

import static win.doyto.query.memory.MemoryDataAccessManager.buildSortingMap;
import static win.doyto.query.memory.MemoryDataAccessManager.sorting;
import static win.doyto.query.util.CommonUtil.*;

/**
 * MemoryDataAccess
 *
 * @author f0rb
 */
@Slf4j
@SuppressWarnings({"unchecked", "java:S3740"})
public class MemoryDataAccess<E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery> implements DataAccess<E, I, Q> {
    protected final Map<I, DataWrapper<E>> entitiesMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final List<Field> fields;
    private final Field idField;
    private final Class<I> idClass;
    @Setter
    private Function<E, DataWrapper<E>> createDataWrapperFunc = SimpleDataWrapper::new;

    public MemoryDataAccess(Class<E> entityClass) {
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

    void loadData(Class<E> entityClass, File root, FileType fileType) {
        TypeReference<FileDataWrapper<E>> typeReference = new TypeReference<>() {
            @Override
            public Type getType() {
                return TypeFactory.defaultInstance().constructParametricType(FileDataWrapper.class, entityClass);
            }
        };

        try {
            String[] files = root.list((dir, name) -> name.endsWith(fileType.getSuffix()));
            for (String filename : files) {
                File file = new File(root, filename);
                FileDataWrapper<E> dataWrapper = fileType.load(file, typeReference);
                dataWrapper.setRoot(root.getPath());
                entitiesMap.put(dataWrapper.get().getId(), dataWrapper);
            }
            long max = entitiesMap.keySet().stream().mapToLong(value -> ((Number) value).longValue()).max().orElse(0);
            idGenerator.set(max);
        } catch (Exception e) {
            throw new FileIOException("Failed to load data from: " + root.getAbsolutePath(), e);
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
        DataWrapper<E> wrapper = entitiesMap.getOrDefault(idWrapper.getId(), DataWrapper.empty());
        return SerializationUtils.clone(wrapper.get());
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
        entitiesMap.put(e.getId(), createDataWrapperFunc.apply(e));
    }

    @Override
    public int update(E e) {
        if (!entitiesMap.containsKey(e.getId())) {
            return 0;
        }
        entitiesMap.put(e.getId(), createDataWrapperFunc.apply(e));
        return 1;
    }

    @Override
    public int patch(E patch) {
        DataWrapper<E> dataWrapper = entitiesMap.get(patch.getId());
        if (dataWrapper == null) {
            return 0;
        }

        E origin = dataWrapper.get();
        for (Field field : fields) {
            Object value = readField(field, patch);
            if (value != null) {
                writeField(field, origin, value);
            }
        }
        entitiesMap.put(origin.getId(), createDataWrapperFunc.apply(origin));
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
        DataWrapper<E> dataWrapper = entitiesMap.remove(idWrapper.getId());
        if (dataWrapper != null) {
            dataWrapper.delete();
            return 1;
        }
        return 0;
    }

    @Override
    public int delete(Q query) {
        List<E> list = query(query);
        list.stream().map(Persistable::getId).forEach(key -> {
            DataWrapper<E> dataWrapper = entitiesMap.remove(key);
            dataWrapper.delete();
        });
        return list.size();
    }

    @Override
    public List<E> query(Q query) {
        Stream<E> stream = filter(query);

        if (query.getSort() != null) {
            LinkedHashMap<String, Integer> sortingMap = buildSortingMap(query.getSort());
            stream = sorting(stream, sortingMap);
        }
        if (query.needPaging()) {
            stream = truncateByPaging(stream, query);
        }
        return stream.map(SerializationUtils::clone).toList();
    }

    @Override
    public long count(Q query) {
        return filter(query).count();
    }

    Stream<E> filter(Query query) {
        BranchConditionNode<E> root = new BranchConditionNode<>(query);
        return entitiesMap.values().stream().map(DataWrapper::get).filter(root);
    }

    public PageList<E> page(Q query) {
        List<E> list = filter(query).toList();
        long count = list.size();
        Stream<E> stream = list.stream();

        if (query.getSort() != null) {
            LinkedHashMap<String, Integer> sortingMap = buildSortingMap(query.getSort());
            stream = sorting(stream, sortingMap);
        }
        if (query.needPaging()) {
            stream = truncateByPaging(stream, query);
        }
        return new PageList<>(stream.map(SerializationUtils::clone).toList(), count);
    }

    private Stream<E> truncateByPaging(Stream<E> stream, Q query) {
        long from = GlobalConfiguration.calcOffset(query);
        return stream.skip(from).limit(query.getPageSize());
    }

    @Override
    @SuppressWarnings("java:S112")
    public <V> List<V> queryColumns(Q q, Class<V> classV, String... columns) {
        List<E> entities = query(q);
        if (columns.length == 1) {
            return entities.stream().map(entity -> (V) readField(entity, columns[0])).toList();
        } else {
            return entities.stream().map(entity -> {
                try {
                    V view = classV.getConstructor().newInstance();
                    copyTo(view, entity, columns);
                    return view;
                } catch (Exception e) {
                    log.error("Failed to convert for {}[{}]:", entity.getClass().getSimpleName(), entity.getId(), e);
                    throw new RuntimeException(e);
                }
            }).toList();
        }
    }

    static void copyTo(Object target, Object source, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Field field = getField(source, fieldName);
            if (field != null) {
                Object value = readField(field, source);
                writeField(field, target, value);
            }
        }
    }

}
