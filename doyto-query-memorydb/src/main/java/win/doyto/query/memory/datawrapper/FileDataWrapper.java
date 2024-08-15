package win.doyto.query.memory.datawrapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import win.doyto.query.entity.Persistable;

import java.io.IOException;

/**
 * FileDataWrapper
 *
 * @author f0rb on 2024/8/12
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileDataWrapper<E extends Persistable<?>> extends SimpleDataWrapper<E> {

    private FileType fileType;
    protected E data;

    @Override
    public E get() {
        return data;
    }

    public DataWrapper<E> flush(String dataRoot) {
        String entityName = data.getClass().getSimpleName();
        String filename = entityName + "#" + data.getId() + fileType.getSuffix();
        String filepath = dataRoot + filename;
        try {
            fileType.write(filepath, this);
        } catch (IOException e) {
            throw new FileIOException("Failed to save " + filepath, e);
        }
        return this;
    }
}