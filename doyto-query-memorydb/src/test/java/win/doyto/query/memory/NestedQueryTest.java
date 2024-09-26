package win.doyto.query.memory;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.PageQuery;
import win.doyto.query.memory.inventory.*;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NestedQueryTest
 *
 * @author f0rb on 2024/8/8
 */
class NestedQueryTest {
    DataAccess<InventoryEntity, Integer, InventoryQuery> inventoryDataAccess;

    @BeforeEach
    void setUp() {
        String path = this.getClass().getResource(File.separator).getPath();
        inventoryDataAccess = MemoryDataAccessManager.create(InventoryEntity.class, path);
    }

    @Test
    void supportNestedPath() {
        InventoryQuery query = InventoryQuery.builder().size(SizeQuery.builder().hLt(14).build()).build();

        List<InventoryEntity> entities = inventoryDataAccess.query(query);
        assertThat(entities).hasSize(3);
    }

    @Test
    void supportNestedPathWithTwoLevels() {
        SizeQuery sizeQuery = SizeQuery.builder().hLt(14).unit(UnitQuery.builder().name("cm").build()).build();
        InventoryQuery query = InventoryQuery.builder().size(sizeQuery).build();

        List<InventoryEntity> entities = inventoryDataAccess.query(query);
        assertThat(entities).hasSize(1);
    }

    @Test
    void supportAggregateForNestedFields() {
        List<InventoryView> entities = MemoryDataAccessManager.aggregate(InventoryView.class, new PageQuery());
        assertThat(entities)
                .extracting("status", "sumQty", "sumHeight")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("A", 120, 32.5),
                        Tuple.tuple("D", 175, 31.35)
                );
    }

}
