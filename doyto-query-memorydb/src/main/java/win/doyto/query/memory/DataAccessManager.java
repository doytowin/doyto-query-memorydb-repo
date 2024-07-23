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
    private final Map<Class<?>, DataAccess<?, ?, ? extends DoytoQuery>> dataAccessMap = new HashMap<>();

    public void register(Class<?> entityClass, DataAccess<?, ?, ? extends DoytoQuery> dataAccess) {
        dataAccessMap.put(entityClass, dataAccess);
    }

    @SuppressWarnings({"unchecked", "java:S1452"})
    public static <Q extends DoytoQuery> List<?> query(Class<?> entityClass, Q query) {
        DataAccess<?, ?, Q> doytoQueryDataAccess = (DataAccess<?, ?, Q>) dataAccessMap.get(entityClass);
        return doytoQueryDataAccess.query(query);
    }
}
