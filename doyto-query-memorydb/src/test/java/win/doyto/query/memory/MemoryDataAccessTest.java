package win.doyto.query.memory;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.PageList;
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
        testMemoryDataAccess = DataAccessManager.create(TestEntity.class);
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
}