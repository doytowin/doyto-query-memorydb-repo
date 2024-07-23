package win.doyto.query.memory.empolyee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.annotation.View;

/**
 * EmployeeView
 *
 * @author f0rb on 2024/7/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@View(EmployeeEntity.class)
public class EmployeeView {

    @GroupBy
    private String department;

    @GroupBy
    private String gender;

    @GroupBy
    private String designation;

    private double avgSalary;

    private double avgBonus;

    private double avgPerks;

}
