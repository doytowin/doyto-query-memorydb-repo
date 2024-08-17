package win.doyto.query.memory.domain.role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.PageQuery;
import win.doyto.query.memory.domain.user.UserQuery;

/**
 * RoleViewQuery
 *
 * @author f0rb on 2024/8/17
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RoleQuery extends PageQuery {
    private String roleName;
    private Boolean valid;
    private UserQuery withCreateUser;
}
