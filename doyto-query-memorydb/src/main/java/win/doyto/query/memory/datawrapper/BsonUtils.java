package win.doyto.query.memory.datawrapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonFactory;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;

/**
 * BsonUtils
 *
 * @author f0rb on 2024/8/12
 */
@UtilityClass
public class BsonUtils {

    private final ObjectMapper objectMapper = new ObjectMapper(new BsonFactory());

    public void write(String filepath, Object target) throws IOException {
        objectMapper.writeValue(new File(filepath), target);
    }

    public static <T> T loadData(File file, TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(file, typeReference);
    }

}
