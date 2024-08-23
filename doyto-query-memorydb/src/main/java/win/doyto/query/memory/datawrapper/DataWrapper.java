package win.doyto.query.memory.datawrapper;

/**
 * DataWrapper
 *
 * @author f0rb on 2024/7/19
 */
public interface DataWrapper<E> {
    E get();

    void delete();

    @SuppressWarnings("unchecked")
    static <T> DataWrapper<T> empty() {
        return (DataWrapper<T>) SimpleDataWrapper.EMPTY;
    }
}
