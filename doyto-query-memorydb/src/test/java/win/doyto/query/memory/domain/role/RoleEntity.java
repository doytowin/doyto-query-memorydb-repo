package win.doyto.query.memory.domain.role;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.entity.AbstractCommonEntity;
import win.doyto.query.memory.domain.user.UserEntity;

/**
 * RoleEntity
 *
 * @author f0rb on 2024/8/17
 */
@Getter
@Setter
public class RoleEntity extends AbstractCommonEntity<Integer, Long> {
    private String roleName;
    private String roleCode;
    private Boolean valid;

    // many-to-one
    @DomainPath(value = "user", localField = "createUserId")
    private UserEntity createUser;
}