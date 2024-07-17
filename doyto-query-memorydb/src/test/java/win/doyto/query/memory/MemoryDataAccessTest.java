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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.test.Account;
import win.doyto.query.test.TestEntity;
import win.doyto.query.test.TestQuery;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static win.doyto.query.test.TestEntity.initUserEntities;

/**
 * MemoryDataAccessTest
 *
 * @author f0rb
 */
class MemoryDataAccessTest {

    MemoryDataAccess<TestEntity, Integer, TestQuery> testMemoryDataAccess;

    @BeforeEach
    void setUp() {
        testMemoryDataAccess = new MemoryDataAccess<>(TestEntity.class);
        testMemoryDataAccess.batchInsert(initUserEntities());
    }

    @Test
    void paging() {
        TestQuery testQuery = TestQuery.builder().pageNumber(2).pageSize(2).build();
        List<TestEntity> entities = testMemoryDataAccess.query(testQuery);
        assertThat(entities).hasSize(2)
                .extracting("id")
                .containsExactly(3, 4);
    }

    @Test
    void filterByUsername() {
        TestQuery testQuery = TestQuery.builder().username("f0rb").build();
        assertEquals(1, testMemoryDataAccess.query(testQuery).size());
    }

    @Test
    void getShouldReturnDifferentEntityObject() {
        TestEntity u1 = testMemoryDataAccess.get(1);
        TestEntity u2 = testMemoryDataAccess.get(1);
        assertNotSame(u1, u2);
    }

    @Test
    void filterByNull() {
        TestQuery byNullMemo = TestQuery.builder().build();
        assertEquals(5, testMemoryDataAccess.count(byNullMemo));

        byNullMemo.setMemoNull(true);
        assertEquals(4, testMemoryDataAccess.count(byNullMemo));

        TestQuery byNoneNullMemo = TestQuery.builder().memoNull(false).build();
        assertEquals(1, testMemoryDataAccess.count(byNoneNullMemo));
    }

    @Test
    void filterByNotInEmptyCollection() {
        TestQuery testQuery = TestQuery.builder().idNotIn(List.of()).build();
        assertEquals(5, testMemoryDataAccess.count(testQuery));
    }

    @Test
    void patch() {
        TestEntity testEntity = new TestEntity();
        testEntity.setMemo("invalid");
        TestQuery byNotValid = TestQuery.builder().valid(false).build();
        testMemoryDataAccess.patch(testEntity, byNotValid);

        assertThat(testMemoryDataAccess.query(byNotValid)).extracting(TestEntity::getMemo).containsExactly("invalid", "invalid");
    }

    @Test
    void sort() {
        TestQuery sort = TestQuery.builder().build();
        sort.setSort("id,desc");
        assertThat(testMemoryDataAccess.query(sort)).extracting(TestEntity::getId).containsExactly(5, 4, 3, 2, 1);

        sort.setSort("valid,asc;id,desc");
        assertThat(testMemoryDataAccess.query(sort)).extracting(TestEntity::getId).containsExactly(3, 1, 5, 4, 2);

        sort.setValid(true);
        assertThat(testMemoryDataAccess.query(sort)).extracting(TestEntity::getId).containsExactly(5, 4, 2);
    }

    @Test
    void shouldReturnZeroAndNotSaveWhenUpdateGivenNonExistEntity() {
        TestEntity e = new TestEntity();
        e.setId(-1);
        int cnt = testMemoryDataAccess.update(e);
        assertEquals(0, cnt);
        assertNull(testMemoryDataAccess.get(-1));
    }

    @Test
    void shouldNotChangeOriginEntityWhenChangeQueryEntity() {
        TestQuery testQuery = TestQuery.builder().build();
        List<TestEntity> entities = testMemoryDataAccess.query(testQuery);
        TestEntity testEntity = entities.get(0);
        testEntity.setMemo("New Memo");

        assertThat(testMemoryDataAccess.get(1).getMemo()).isNotEqualTo("New Memo");
    }

    @Test
    void supportSuffixOrWithCustomType() {
        Account accountOr = Account.builder().username("f0rb").email("f0rb").build();
        TestQuery testQuery = TestQuery.builder().accountOr(accountOr).build();
        List<TestEntity> entities = testMemoryDataAccess.query(testQuery);
        assertThat(entities).hasSize(1);
    }

    @Test
    void shouldNotKeepEmptyOrNode() {
        Account accountOr = Account.builder().build();
        TestQuery testQuery = TestQuery.builder().accountOr(accountOr).build();
        List<TestEntity> entities = testMemoryDataAccess.query(testQuery);
        assertThat(entities).hasSize(5);
    }

    @Test
    void supportSuffixOrWithCollectionWithBasicType() {
        TestQuery testQuery = TestQuery.builder().usernameContainOr(Arrays.asList("name1", "name3")).build();
        List<TestEntity> entities = testMemoryDataAccess.query(testQuery);
        assertThat(entities).hasSize(2);
    }
}