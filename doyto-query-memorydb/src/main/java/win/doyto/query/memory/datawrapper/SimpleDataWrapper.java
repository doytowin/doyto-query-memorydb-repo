package win.doyto.query.memory.datawrapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SimpleDataWrapper
 *
 * @author f0rb on 2024/7/19
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleDataWrapper<E> implements DataWrapper<E> {

    static final DataWrapper<?> EMPTY = new SimpleDataWrapper<>(null);

    protected E data;

    @Override
    public E get() {
        return data;
    }

    @Override
    public void delete() {
        //nothing to delete
    }
}
