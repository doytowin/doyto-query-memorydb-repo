package win.doyto.query.memory.inventory;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.AbstractPersistable;


/**
 * InventoryEntity
 *
 * @author f0rb on 2021-11-23
 */
@Getter
@Setter
public class InventoryEntity extends AbstractPersistable<Integer> {

    private String item;
    private Integer qty;
    private InventorySize size;
    private String status;

}
