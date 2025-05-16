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

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.PageList;
import win.doyto.query.memory.datawrapper.FileType;
import win.doyto.query.memory.empolyee.EmployeeEntity;
import win.doyto.query.memory.empolyee.EmployeeQuery;
import win.doyto.query.test.Account;
import win.doyto.query.test.TestEntity;
import win.doyto.query.test.TestQuery;

import java.io.File;
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
    String path = MemoryDataAccessTest.class.getResource(File.separator).getPath();

    @BeforeEach
    void setUp() {
        MemoryDataAccessManager.dataAccessMap.remove(TestEntity.class);
        MemoryDataAccessManager.dataAccessMap.remove(EmployeeEntity.class);
        testMemoryDataAccess = MemoryDataAccessManager.create(TestEntity.class);
        testMemoryDataAccess.batchInsert(initUserEntities());
    }

    @Test
    void page() {
        TestQuery testQuery = TestQuery.builder().pageNumber(2).pageSize(2).build();
        PageList<TestEntity> page = testMemoryDataAccess.page(testQuery);
        List<TestEntity> entities = page.getList();
        assertThat(page.getTotal()).isEqualTo(5);
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
    void queryIds() {
        TestQuery testQuery = TestQuery.builder().build();
        List<Integer> idList = testMemoryDataAccess.queryIds(testQuery);
        assertThat(idList).hasSize(5)
                          .containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void queryColumns() {
        TestQuery testQuery = TestQuery.builder().build();
        List<TestEntity> entities = testMemoryDataAccess.queryColumns(testQuery,
                TestEntity.class, "id", "username", "pass");
        assertThat(entities).hasSize(5)
                            .extracting("id", "username", "password")
                            .containsExactly(
                                    Tuple.tuple(1, "username1", null),
                                    Tuple.tuple(2, "username2", null),
                                    Tuple.tuple(3, "username3", null),
                                    Tuple.tuple(4, "username4", null),
                                    Tuple.tuple(5, "f0rb", null)
                            );
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
    void deleteById() {
        int cnt = testMemoryDataAccess.delete(1);
        assertThat(cnt).isEqualTo(1);

        cnt = testMemoryDataAccess.delete(1);
        assertThat(cnt).isZero();
    }

    @Test
    void deleteByQuery() {
        TestQuery testQuery = TestQuery.builder().usernameContain("user").build();
        int cnt = testMemoryDataAccess.delete(testQuery);
        assertThat(cnt).isEqualTo(4);
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

    @Test
    void supportSuffixOrWithCollectionWithCustomType() {
        Account account1 = Account.builder().username("username1").email("test1@163.com").build();
        Account account2 = Account.builder().email("test4@163.com").build();

        TestQuery testQuery = TestQuery.builder().accountsOr(Arrays.asList(account1, account2)).build();
        List<TestEntity> entities = testMemoryDataAccess.query(testQuery);
        assertThat(entities).hasSize(2);
    }


    @Test
    void supportSuffixAndWithQueryType() {
        MemoryDataAccess<EmployeeEntity, Integer, DoytoQuery> employeeDataAccess
                = MemoryDataAccessManager.create(EmployeeEntity.class, path, FileType.JSON);

        EmployeeQuery empAnd = EmployeeQuery.builder().gender("male").idGe(3).build();
        EmployeeQuery employeeQuery = EmployeeQuery.builder().empAnd(empAnd).build();
        List<EmployeeEntity> entities = employeeDataAccess.query(employeeQuery);
        assertThat(entities).hasSize(2);
    }

    @Test
    void shouldDeleteFileWhenDeleteById() {
        MemoryDataAccess<EmployeeEntity, Integer, DoytoQuery> employeeDataAccess
                = MemoryDataAccessManager.create(EmployeeEntity.class, path, FileType.JSON);

        employeeDataAccess.create(new EmployeeEntity());
        File file = new File(path, "EmployeeEntity" + File.separator + "EmployeeEntity#7.json");
        assertThat(file).exists();

        employeeDataAccess.delete(7);
        assertThat(file).doesNotExist();
    }

    @Test
    void shouldDeleteFileWhenDeleteByQuery() {
        MemoryDataAccess<EmployeeEntity, Integer, DoytoQuery> employeeDataAccess
                = MemoryDataAccessManager.create(EmployeeEntity.class, path, FileType.JSON);

        employeeDataAccess.create(new EmployeeEntity());
        employeeDataAccess.create(new EmployeeEntity());
        File file7 = new File(path, "EmployeeEntity" + File.separator + "EmployeeEntity#7.json");
        File file8 = new File(path, "EmployeeEntity" + File.separator + "EmployeeEntity#8.json");
        assertThat(file7).exists();
        assertThat(file8).exists();

        employeeDataAccess.delete(EmployeeQuery.builder().idGe(7).build());
        assertThat(file7).doesNotExist();
        assertThat(file8).doesNotExist();
    }

    @Test
    void supportSubquery() {
        MemoryDataAccess<EmployeeEntity, Integer, DoytoQuery> employeeDataAccess
                = MemoryDataAccessManager.create(EmployeeEntity.class, path, FileType.JSON);

        EmployeeQuery query = EmployeeQuery.builder().gender("male").salaryGt(EmployeeQuery.builder().build()).build();
        List<EmployeeEntity> entities = employeeDataAccess.query(query);
        assertThat(entities)
                .extracting("id", "gender", "salary")
                .containsExactlyInAnyOrder(
                        Tuple.tuple(1, "male", 100000),
                        Tuple.tuple(2, "male", 80000)
                );
    }
}