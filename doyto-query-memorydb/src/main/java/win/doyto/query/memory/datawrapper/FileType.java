package win.doyto.query.memory.datawrapper;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import win.doyto.query.entity.Persistable;
import win.doyto.query.util.BeanUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * FileType
 *
 * @author f0rb on 2024/8/15
 */
@AllArgsConstructor
public enum FileType {
    JSON(".json", (filepath, target) -> FileUtils.write(new File(filepath), BeanUtil.stringify(target), StandardCharsets.UTF_8),
            new FileLoader() {
                @Override
                public <E extends Persistable<?>> FileDataWrapper<E>
                load(File file, TypeReference<FileDataWrapper<E>> typeReference) throws IOException {
                    return BeanUtil.loadJsonData(new FileInputStream(file), typeReference);
                }
            }
    ),
    BSON(".bson", BsonUtils::write, BsonUtils::loadData),
    ;

    @Getter
    private final String suffix;
    private final FileWriter fileWriter;
    private final FileLoader fileLoader;

    public void write(String filepath, Object target) throws IOException {
        fileWriter.write(filepath, target);
    }

    public <E extends Persistable<I>, I extends Serializable> FileDataWrapper
    load(File file, TypeReference<FileDataWrapper<E>> typeReference) throws IOException {
        return fileLoader.load(file, typeReference);
    }

    /**
     * FileWriter
     *
     * @author f0rb on 2024/8/15
     */
    interface FileWriter {
        void write(String filepath, Object target) throws IOException;
    }

    /**
     * FileLoader
     *
     * @author f0rb on 2024/8/15
     */
    interface FileLoader {
        <E extends Persistable<?>> FileDataWrapper<E>
        load(File file, TypeReference<FileDataWrapper<E>> typeReference) throws IOException;
    }
}
