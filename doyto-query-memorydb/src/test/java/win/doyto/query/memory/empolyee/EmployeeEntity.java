package win.doyto.query.memory.empolyee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import win.doyto.query.entity.AbstractPersistable;

/**
 * EmployeeEntity
 *
 * @author f0rb on 2024/7/22
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEntity extends AbstractPersistable<Integer> {

    private String name;

    private String department;

    private String gender;

    private String designation;

    private Integer salary;

    private Integer bonus;

    private Integer perks;

}