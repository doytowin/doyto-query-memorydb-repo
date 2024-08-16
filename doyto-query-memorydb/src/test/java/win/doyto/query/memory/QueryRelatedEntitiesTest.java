package win.doyto.query.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.DataAccess;
import win.doyto.query.memory.domain.user.UserEntity;
import win.doyto.query.memory.domain.user.UserQuery;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QueryRelatedEntitiesTest
 *
 * @author f0rb on 2024/8/13
 */
public class QueryRelatedEntitiesTest {
    DataAccess<UserEntity, Long, UserQuery> userDataAccess = MemoryDataAccessManager.create(UserEntity.class);

    public static List<UserEntity> initUserEntities() {
        List<UserEntity> userEntities = new ArrayList<>(5);

        for (int i = 1; i <= 5; ++i) {
            UserEntity userEntity = new UserEntity();
            userEntity.setId((long) i);
            userEntity.setUsername("username" + i);
            userEntity.setPassword("password" + i);
            userEntity.setEmail("test" + i + "@163.com");
            userEntity.setMobile("1777888888" + i);
            userEntity.setValid(i % 2 == 0);
            userEntity.setScore(i * 10);
            userEntity.setCreateUserId(1L);
            userEntities.add(userEntity);
        }
        userEntities.get(0).setCreateUserId(0L);
        return userEntities;
    }

    @BeforeEach
    void setUp() {
        List<UserEntity> userEntities = initUserEntities();
        userDataAccess.batchInsert(userEntities);
    }

    @Test
    void queryUserWithCreatedUsersAndCreateUser() {
        UserQuery createdUsersQuery = UserQuery.builder().build();
        UserQuery createUserQuery = UserQuery.builder().build();
        UserQuery userQuery = UserQuery.builder()
                                       .withCreatedUsers(createdUsersQuery)
                                       .withCreateUser(createUserQuery)
                                       .pageSize(10).build();
        List<UserEntity> users = userDataAccess.query(userQuery);

        assertThat(users).hasSize(5);
        assertThat(users.get(0).getCreatedUsers()).hasSize(4);
        assertThat(users.get(1).getCreatedUsers()).isEmpty();
        assertThat(users.get(2).getCreatedUsers()).isEmpty();

        assertThat(users.get(0).getCreateUser()).isNull();
        assertThat(users.get(1).getCreateUser().getId()).isEqualTo(1);
        assertThat(users.get(2).getCreateUser().getId()).isEqualTo(1);
    }
}
