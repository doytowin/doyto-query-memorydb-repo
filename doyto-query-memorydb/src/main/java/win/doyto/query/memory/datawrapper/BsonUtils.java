/*
 * Copyright © 2022-2025 DoytoWin, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
