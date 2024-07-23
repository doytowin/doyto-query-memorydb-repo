package win.doyto.query.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.memory.empolyee.EmployeeEntity;
import win.doyto.query.memory.empolyee.EmployeeQuery;
import win.doyto.query.memory.empolyee.EmployeeView;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MemoryQueryClientTest
 *
 * @author f0rb on 2024/7/22
 */
class MemoryQueryClientTest {
    MemoryQueryClient queryClient = new MemoryQueryClient();

    /**
     * The data comes from <a href="https://stackoverflow.com/questions/72038822/aggregate-multiple-fields-grouping-by-multiple-fields-in-java-8">here</a>.
     */
    @BeforeEach
    void setUp() {
        MemoryDataAccess<EmployeeEntity, Integer, EmployeeQuery> dataAccess = new MemoryDataAccess<>(EmployeeEntity.class);
        DataAccessManager.register(EmployeeEntity.class, dataAccess);
        List<EmployeeEntity> employees = new ArrayList<>();
        employees.add(new EmployeeEntity("bill", "dep1", "male", "des1", 100000, 5000, 20));
        employees.add(new EmployeeEntity("john", "dep1", "male", "des1", 80000, 4000, 10));
        employees.add(new EmployeeEntity("lisa", "dep1", "female", "des1", 80000, 4000, 10));
        employees.add(new EmployeeEntity("rosie", "dep1", "female", "des2", 70000, 3000, 13));
        employees.add(new EmployeeEntity("will", "dep2", "male", "des1", 60000, 3500, 18));
        employees.add(new EmployeeEntity("murray", "dep2", "male", "des1", 70000, 3000, 13));
        dataAccess.batchInsert(employees);
    }

    @Test
    void aggregate() {
        List<EmployeeView> testViews = queryClient.aggregate(new EmployeeQuery(), EmployeeView.class);

        assertThat(testViews).hasSize(4).contains(
                new EmployeeView("dep1", "male", "des1", 2, 90000, 4500, 15.0, 100000, 80000, 20, 10, 30),
                new EmployeeView("dep1", "female", "des2", 1, 70000, 3000, 13.0, 70000, 70000, 13, 13, 13),
                new EmployeeView("dep1", "female", "des1", 1, 80000, 4000, 10.0, 80000, 80000, 10, 10, 10),
                new EmployeeView("dep2", "male", "des1", 2, 65000, 3250, 15.5, 70000, 60000, 18, 13, 31)
        );

    }

}