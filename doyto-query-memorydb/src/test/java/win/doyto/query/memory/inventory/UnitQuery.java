package win.doyto.query.memory.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.Query;

/**
 * UnitQuery
 *
 * @author f0rb on 2021-12-04
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UnitQuery implements Query {
    private String name;
}