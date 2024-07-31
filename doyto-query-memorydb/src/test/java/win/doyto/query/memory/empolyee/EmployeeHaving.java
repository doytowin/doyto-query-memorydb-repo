package win.doyto.query.memory.empolyee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.Having;

/**
 * EmployeeHaving
 *
 * @author f0rb on 2024/7/31
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeHaving implements Having {
    private Integer avgBonusGe;
}
