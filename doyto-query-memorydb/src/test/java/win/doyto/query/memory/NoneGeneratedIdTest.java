/*
 * Copyright © 2019-2024 Forb Yuan
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

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import win.doyto.query.annotation.Id;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NoneGeneratedIdTest
 *
 * @author f0rb on 2024/7/17
 */
class NoneGeneratedIdTest {

    @Test
    void createNoneGeneratedIdEntity() {

        MemoryDataAccess<NoneGeneratedIdEntity, Integer, PageQuery> testMemoryDataAccess
                = new MemoryDataAccess<>(NoneGeneratedIdEntity.class);

        NoneGeneratedIdEntity noneGeneratedIdEntity = new NoneGeneratedIdEntity();
        noneGeneratedIdEntity.setId(1);
        noneGeneratedIdEntity.setTest("some");
        testMemoryDataAccess.create(noneGeneratedIdEntity);

        assertThat(testMemoryDataAccess.count(new PageQuery())).isEqualTo(1);
    }

    @Getter
    @Setter
    public class NoneGeneratedIdEntity implements Persistable<Integer> {
        @Id
        protected Integer id;
        private String test;
    }
}
