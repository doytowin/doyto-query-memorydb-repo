package win.doyto.query.memory.aggregate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * AggregateMetadata
 *
 * @author f0rb on 2024/7/27
 */
public interface AggregateMetadata {

    Field getField();

    String getLabel();

    default void accumulate(Map<String, List<Object>> map, Object entity) {
    }

    /**
     * Compute the aggregate result of the input values.
     *
     * @param efvMap a map of the fields value list.
     * @return aggregate result.
     */
    Object execute(Map<String, List<Object>> efvMap);

}
