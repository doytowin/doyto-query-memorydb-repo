package win.doyto.query.memory.datawrapper;

import lombok.NoArgsConstructor;
import win.doyto.query.entity.Persistable;

import java.io.IOException;

/**
 * BsonFileDataWrapper
 *
 * @author f0rb on 2024/8/12
 */
@NoArgsConstructor
public class BsonFileDataWrapper<E extends Persistable<?>> extends SimpleDataWrapper<E> {

    public BsonFileDataWrapper(E entity) {
        super(entity);
    }

    public DataWrapper<E> flush(String dataRoot) {
        String entityName = data.getClass().getSimpleName();
        String filename = entityName + "#" + data.getId() + ".bson";
        String filepath = dataRoot + filename;
        try {
            BsonUtils.write(filepath, this);
        } catch (IOException e) {
            throw new FileIOException("Failed to save " + filepath, e);
        }
        return this;
    }
}