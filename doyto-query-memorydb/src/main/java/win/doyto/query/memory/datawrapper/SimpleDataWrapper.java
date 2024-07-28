package win.doyto.query.memory.datawrapper;

import lombok.AllArgsConstructor;

/**
 * SimpleDataWrapper
 *
 * @author f0rb on 2024/7/19
 */
@AllArgsConstructor
public class SimpleDataWrapper<E> implements DataWrapper<E> {

    E data;

    @Override
    public E get() {
        return data;
    }
}
