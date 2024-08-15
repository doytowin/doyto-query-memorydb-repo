package win.doyto.query.memory.datamapper;

import lombok.AllArgsConstructor;
import win.doyto.query.util.BeanUtil;

import java.util.Map;

/**
 * DefaultDataMapper
 *
 * @author f0rb on 2024/8/15
 */
@AllArgsConstructor
public class DefaultDataMapper<V> implements DataMapper<V>{

    private final Class<V> viewClass;

    @Override
    public V map(Map<String, Object> map) {
        return BeanUtil.convertTo(map, viewClass);
    }
}
