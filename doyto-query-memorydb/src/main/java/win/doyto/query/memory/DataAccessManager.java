package win.doyto.query.memory;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.memory.aggregate.Aggregation;
import win.doyto.query.memory.datawrapper.BsonFileDataWrapper;
import win.doyto.query.memory.datawrapper.FileIOException;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static win.doyto.query.util.CommonUtil.readField;
import static win.doyto.query.util.CommonUtil.toCamelCase;

/**
 * DataAccessManager
 *
 * @author f0rb on 2024/7/23
 */
@UtilityClass
public class DataAccessManager {
    private final Map<Class<?>, DataAccess<?, ?, ? super DoytoQuery>> dataAccessMap = new ConcurrentHashMap<>();
    public final MemoryQueryClient CLIENT = new MemoryQueryClient();

    public synchronized <E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    MemoryDataAccess<E, I, Q> create(Class<E> entityClass) {
        return create(entityClass, null);
    }

    @SuppressWarnings({"unchecked"})
    public synchronized <E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    MemoryDataAccess<E, I, Q> create(Class<E> entityClass, String store) {
        MemoryDataAccess<E, I, DoytoQuery> dataAccess = new MemoryDataAccess<>(entityClass);
        if (store != null) {
            String dataRoot = store + entityClass.getSimpleName() + File.separator;
            dataAccess.setCreateDataWrapperFunc(e -> new BsonFileDataWrapper<>(e).flush(dataRoot));
            File root = new File(dataRoot);
            if (!root.exists() && !root.mkdirs()) {
                throw new FileIOException("Failed to create data directory: " + dataRoot);
            }
            dataAccess.loadData(entityClass, root);
        }

        dataAccessMap.put(entityClass, dataAccess);
        return (MemoryDataAccess<E, I, Q>) dataAccess;
    }

    @SuppressWarnings({"unchecked"})
    public static <E, Q extends DoytoQuery> List<E> query(Class<E> entityClass, Q query) {
        DataAccess<?, ?, ? super DoytoQuery> doytoQueryDataAccess = dataAccessMap.get(entityClass);
        return (List<E>) doytoQueryDataAccess.query(query);
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
}
