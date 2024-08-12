package win.doyto.query.memory.empolyee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.AggregatedQuery;

/**
 * EmployeeAggrQuery
 *
 * @author f0rb on 2024/7/31
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAggrQuery extends AggregatedQuery {
    private Integer avgBonusGe;
    private EmployeeQuery entityQuery;
}
