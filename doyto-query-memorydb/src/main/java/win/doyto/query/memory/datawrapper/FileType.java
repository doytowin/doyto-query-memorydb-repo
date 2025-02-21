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

    public <E extends Persistable<I>, I extends Serializable>
    FileDataWrapper<E> load(File file, TypeReference<FileDataWrapper<E>> typeReference) throws IOException {
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
