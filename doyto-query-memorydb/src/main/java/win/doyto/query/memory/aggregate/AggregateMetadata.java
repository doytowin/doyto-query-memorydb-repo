package win.doyto.query.memory.aggregate;

import java.lang.reflect.Field;
import java.util.List;

/**
 * AggregateMetadata
 *
 * @author f0rb on 2024/7/27
 */
public interface AggregateMetadata {

    Field getField();

    String getLabel();

    /**
     * Compute the aggregate result of the input values.
     *
     * @param efvList a value list of an entity field.
     * @return aggregate result.
     */
    Object execute(List<Object> efvList);

}
