package win.doyto.query.memory.domain.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.entity.AbstractPersistable;
import win.doyto.query.validation.CreateGroup;

import java.util.List;

/**
 * UserEntity
 *
 * @author f0rb on 2024/8/15
 */
@Getter
@Setter
public class UserEntity extends AbstractPersistable<Long> {
    @NotNull(groups = CreateGroup.class)
    private String username;
    private String email;
    private String mobile;

    @NotNull(groups = CreateGroup.class)
    private String password;
    private String nickname;
    private Boolean valid;
    private String memo;
    private Integer score;
    private Long createUserId;

    @DomainPath(
            value = {"user"},
            localField = "createUserId"
    )
    private UserEntity createUser;

    @DomainPath(
            value = {"user"},
            foreignField = "createUserId"
    )
    private List<UserEntity> createdUsers;

}
