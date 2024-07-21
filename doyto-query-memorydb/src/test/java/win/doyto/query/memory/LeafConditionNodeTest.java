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

import org.junit.jupiter.api.Test;
import win.doyto.query.test.TestEntity;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LeafConditionNodeTest
 *
 * @author f0rb on 2024/7/21
 */
class LeafConditionNodeTest {

    @Test
    void supportRx() {
        LeafConditionNode<Object> leaf = new LeafConditionNode<>("usernameRx", "my$");
        TestEntity testEntity = TestEntity.builder().username("Jimmy").build();
        boolean result = leaf.test(testEntity);
        assertTrue(result);
    }

}