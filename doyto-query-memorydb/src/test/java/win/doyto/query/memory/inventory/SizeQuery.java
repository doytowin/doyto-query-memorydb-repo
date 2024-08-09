package win.doyto.query.memory.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.Query;

/**
 * SizeQuery
 *
 * @author f0rb on 2021-11-24
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SizeQuery implements Query {
    @JsonProperty("hLt")
    private Integer hLt;
    private String uom;
    private UnitQuery unit;
}
