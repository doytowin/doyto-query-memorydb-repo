package win.doyto.query.memory.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * InventorySize
 *
 * @author f0rb on 2021-11-23
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventorySize implements Serializable {
    private Double h;
    private Double w;
    private String uom;
    private InventoryUnit unit;
}
