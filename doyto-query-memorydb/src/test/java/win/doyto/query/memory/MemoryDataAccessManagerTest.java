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

package win.doyto.query.memory;

import org.junit.jupiter.api.Test;
import win.doyto.query.memory.datawrapper.FileIOException;
import win.doyto.query.test.TestEntity;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MemoryDataAccessManagerTest
 *
 * @author f0rb on 2024/8/12
 */
class MemoryDataAccessManagerTest {

    @Test
    void shouldFailWhenEntityDataDirNotExist() {
        try {
            String store = "/none/exist/path/";
            MemoryDataAccessManager.create(TestEntity.class, store);
            File root = new File(store + "TestEntity");
            assertTrue(root.exists());
        } catch (Exception e) {
            assertEquals(FileIOException.class, e.getClass());
        }

    }

}