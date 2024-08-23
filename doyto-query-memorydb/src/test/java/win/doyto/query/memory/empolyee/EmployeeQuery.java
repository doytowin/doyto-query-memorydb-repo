package win.doyto.query.memory.empolyee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.Subquery;
import win.doyto.query.core.PageQuery;

/**
 * EmployeeQuery
 *
 * @author f0rb on 2024/7/22
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeQuery extends PageQuery {
    private Integer idGe;
    private String gender;
    @Subquery(select = "avg(salary)", from = EmployeeEntity.class)
    private EmployeeQuery salaryGt;
}
