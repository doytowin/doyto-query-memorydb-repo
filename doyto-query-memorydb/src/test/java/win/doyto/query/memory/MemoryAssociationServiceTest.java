/*
 * Copyright © 2022-2026 DoytoWin, Inc.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.UniqueKey;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MemoryAssociationServiceTest
 *
 * @author f0rb on 2024/8/12
 */
class MemoryAssociationServiceTest {

    private MemoryAssociationService<Long, Integer> userRoleAstService;

    @BeforeEach
    void setUp() {
        userRoleAstService = new MemoryAssociationService<>("user", "role");
        userRoleAstService.associate(1L, 1);
        userRoleAstService.associate(1L, 2);
        userRoleAstService.associate(1L, 3);
        userRoleAstService.associate(2L, 2);
        userRoleAstService.associate(2L, 4);
    }

    @Test
    void dissociate() {
        assertThat(userRoleAstService.dissociate(2L, 2)).isEqualTo(1);
        assertThat(userRoleAstService.dissociate(2L, 5)).isZero();
    }

    @Test
    void queryK1ByK2() {
        List<Long> k1List = userRoleAstService.queryK1ByK2(2);
        assertThat(k1List).containsExactly(1L, 2L);
    }

    @Test
    void queryK2ByK1() {
        List<Integer> k2List = userRoleAstService.queryK2ByK1(1L);
        assertThat(k2List).containsExactly(1, 2, 3);
    }

    @Test
    void deleteByK1() {
        int count = userRoleAstService.deleteByK1(1L);
        assertThat(count).isEqualTo(3);
        assertThat(userRoleAstService.queryK2ByK1(1L)).isEmpty();
    }

    @Test
    void deleteByK2() {
        int count = userRoleAstService.deleteByK2(3);
        assertThat(count).isEqualTo(1);
        assertThat(userRoleAstService.queryK1ByK2(3)).isEmpty();
    }

    @Test
    void count() {
        Set<UniqueKey<Long, Integer>> uniqueKeys = userRoleAstService.buildUniqueKeys(1L, Arrays.asList(1, 4, 5));
        long count = userRoleAstService.count(uniqueKeys);
        assertThat(count).isEqualTo(1);
    }
}