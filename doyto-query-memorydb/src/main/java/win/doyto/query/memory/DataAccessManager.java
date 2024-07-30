package win.doyto.query.memory;

import lombok.experimental.UtilityClass;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.DoytoQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataAccessManager
 *
 * @author f0rb on 2024/7/23
 */
@UtilityClass
public class DataAccessManager {
    private final Map<Class<?>, DataAccess<?, ?, ? super DoytoQuery>> dataAccessMap = new HashMap<>();
    public final MemoryQueryClient CLIENT = new MemoryQueryClient();

    public void register(Class<?> entityClass, DataAccess<?, ?, ? super DoytoQuery> dataAccess) {
        dataAccessMap.put(entityClass, dataAccess);
    }

    @SuppressWarnings({"unchecked"})
    public static <E, Q extends DoytoQuery> List<E> query(Class<E> entityClass, Q query) {
        DataAccess<?, ?, ? super DoytoQuery> doytoQueryDataAccess = dataAccessMap.get(entityClass);
        return (List<E>) doytoQueryDataAccess.query(query);
    }
}
