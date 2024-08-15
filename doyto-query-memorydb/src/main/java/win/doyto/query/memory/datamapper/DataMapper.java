package win.doyto.query.memory.datamapper;

import java.util.Map;

/**
 * DataMapper
 *
 * @author f0rb on 2024/8/15
 */
public interface DataMapper<V> {
    V map(Map<String, Object> map);
}
