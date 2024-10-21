package win.doyto.query.memory.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.PageQuery;
import win.doyto.query.memory.domain.role.RoleQuery;

/**
 * UserQuery
 *
 * @author f0rb on 2024/8/15
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserQuery extends PageQuery {
    private Long id;
    private String username;
    private String email;
    private Long createUserId;

    private UserQuery withCreatedUsers;
    private UserQuery withCreateUser;
    private RoleQuery withRoles;
}
