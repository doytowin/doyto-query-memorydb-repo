package win.doyto.query.memory.datawrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import win.doyto.query.entity.Persistable;

import java.io.File;
import java.io.IOException;

/**
 * FileDataWrapper
 *
 * @author f0rb on 2024/8/12
 */
@SuppressWarnings("java:S2065")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileDataWrapper<E extends Persistable<?>> implements DataWrapper<E> {

    protected E data;
    private FileType fileType;
    @JsonIgnore
    private transient String root;

    @Override
    public E get() {
        return data;
    }

    public DataWrapper<E> flush() {
        String filepath = makeFilepath();
        try {
            fileType.write(filepath, this);
        } catch (IOException e) {
            throw new FileIOException("Failed to save " + filepath, e);
        }
        return this;
    }

    @Override
    public void delete() {
        String filepath = makeFilepath();
        FileUtils.deleteQuietly(new File(filepath));
    }

    private String makeFilepath() {
        String entityName = data.getClass().getSimpleName();
        String filename = entityName + "#" + data.getId() + fileType.getSuffix();
        return root + File.separator + filename;
    }
}