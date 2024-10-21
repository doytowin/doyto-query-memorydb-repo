package win.doyto.query.memory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.DataAccess;
import win.doyto.query.memory.domain.role.RoleEntity;
import win.doyto.query.memory.domain.role.RoleQuery;
import win.doyto.query.memory.domain.user.UserEntity;
import win.doyto.query.memory.domain.user.UserQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QueryRelatedEntitiesTest
 *
 * @author f0rb on 2024/8/13
 */
class QueryRelatedEntitiesTest {
    static DataAccess<UserEntity, Long, UserQuery> userDataAccess = MemoryDataAccessManager.create(UserEntity.class);
    static DataAccess<RoleEntity, Integer, RoleQuery> roleDataAccess = MemoryDataAccessManager.create(RoleEntity.class);
    static MemoryAssociationService<Long, Integer> userRoleAstService;

    public static List<UserEntity> initUserEntities() {
        List<UserEntity> userEntities = new ArrayList<>(5);

        for (int i = 1; i <= 5; ++i) {
            UserEntity userEntity = new UserEntity();
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

    public static List<RoleEntity> initRoleEntities() {
        List<RoleEntity> roleEntities = new ArrayList<>();

        for (int i = 1; i <= 6; ++i) {
            RoleEntity roleEntity = new RoleEntity();
            roleEntity.setRoleName("vip" + i);
            roleEntity.setRoleCode("VIP" + i);
            roleEntity.setValid(i % 4 == 0);
            if (i < 3) {
                roleEntity.setCreateUserId(1L);
            } else {
                roleEntity.setCreateUserId(3L);
            }
            roleEntities.add(roleEntity);
        }
        roleEntities.get(0).setCreateUserId(0L);
        return roleEntities;
    }

    @BeforeAll
    static void beforeAll() {
        List<UserEntity> userEntities = initUserEntities();
        userDataAccess.batchInsert(userEntities);

        List<RoleEntity> roleEntities = initRoleEntities();
        roleDataAccess.batchInsert(roleEntities);

        MemoryDataAccessManager.register("user", "role");
        userRoleAstService = MemoryDataAccessManager.getAstService("user", "role");
        userRoleAstService.reassociateForK1(1L, Arrays.asList(1, 2, 3));
        userRoleAstService.reassociateForK1(2L, Arrays.asList(2, 4));
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

    @Test
    void queryRolesWithCreateUser() {
        UserQuery createUserQuery = UserQuery.builder().build();
        RoleQuery roleQuery = RoleQuery.builder()
                                       .withCreateUser(createUserQuery)
                                       .pageSize(10).build();
        List<RoleEntity> roles = roleDataAccess.query(roleQuery);

        assertThat(roles).hasSize(6);
        assertThat(roles.get(1).getCreateUser().getId()).isEqualTo(1);
        assertThat(roles.get(2).getCreateUser().getId()).isEqualTo(3);
        assertThat(roles.get(4).getCreateUser().getId()).isEqualTo(3);
    }

    @Test
    void queryUserWithRoles() {
        UserQuery userQuery = UserQuery.builder().withRoles(RoleQuery.builder().build()).build();
        List<UserEntity> users = userDataAccess.query(userQuery);
        assertThat(users).hasSize(5);
        assertThat(users.get(0).getRoles()).hasSize(3);
        assertThat(users.get(1).getRoles()).hasSize(2);
        assertThat(users.get(2).getRoles()).isEmpty();
    }

    @Test
    void queryUserWithVip1Role() {
        RoleQuery roleQuery = RoleQuery.builder().roleName("vip1").build();
        UserQuery userQuery = UserQuery.builder().withRoles(roleQuery).build();
        List<UserEntity> users = userDataAccess.query(userQuery);
        assertThat(users).hasSize(5);
        assertThat(users.get(0).getRoles()).hasSize(1);
        assertThat(users.get(1).getRoles()).isEmpty();
    }

    @DisplayName("Support reverse path")
    @Test
    void queryRoleWithUsers() {
        RoleQuery roleQuery = RoleQuery.builder().withUsers(new UserQuery()).pageSize(10).build();
        List<RoleEntity> roles = roleDataAccess.query(roleQuery);

        assertThat(roles).hasSize(6);
        assertThat(roles.get(0).getUsers()).hasSize(1);
        assertThat(roles.get(1).getUsers()).hasSize(2);
        assertThat(roles.get(2).getUsers()).hasSize(1);
        assertThat(roles.get(5).getUsers()).isEmpty();
    }
}
