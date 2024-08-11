package win.doyto.query.memory.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import win.doyto.query.annotation.Column;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.annotation.View;

/**
 * InventoryView
 *
 * @author f0rb on 2024/8/11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@View(InventoryEntity.class)
public class InventoryView {
    @GroupBy
    private String status;
    @Column(name = "sum(size.h)")
    private Double sumHeight;
    private Integer sumQty;
}
