package win.doyto.query.memory;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.memory.empolyee.EmployeeEntity;
import win.doyto.query.memory.empolyee.EmployeeHaving;
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

    /**
     * The data comes from <a href="https://stackoverflow.com/questions/72038822/aggregate-multiple-fields-grouping-by-multiple-fields-in-java-8">here</a>.
     */
    @BeforeAll
    static void beforeAll() {
        MemoryDataAccess<EmployeeEntity, Integer, DoytoQuery> empDataAccess = new MemoryDataAccess<>(EmployeeEntity.class);
        DataAccessManager.register(EmployeeEntity.class, empDataAccess);
        List<EmployeeEntity> employees = new ArrayList<>();
        employees.add(new EmployeeEntity("bill", "dep1", "male", "des1", 100000, 5000, 20));
        employees.add(new EmployeeEntity("john", "dep1", "male", "des1", 80000, 4000, 10));
        employees.add(new EmployeeEntity("lisa", "dep1", "female", "des1", 80000, 4000, 10));
        employees.add(new EmployeeEntity("rosie", "dep1", "female", "des2", 70000, 3000, 13));
        employees.add(new EmployeeEntity("will", "dep2", "male", "des1", 60000, 3500, 18));
        employees.add(new EmployeeEntity("murray", "dep2", "male", "des1", 70000, 3000, 13));
        empDataAccess.batchInsert(employees);
    }

    @Test
    void aggregate() {
        EmployeeQuery query = EmployeeQuery.builder().sort("maxSalary,desc;avgSalary").build();
        List<EmployeeView> testViews = DataAccessManager.CLIENT.aggregate(query, EmployeeView.class);

        assertThat(testViews).hasSize(4).containsExactly(
                new EmployeeView("dep1", "male", "des1", 2, 90000, 4500, 15.0, 100000, 80000, 20, 10, 30, 94500, 1.05),
                new EmployeeView("dep1", "female", "des1", 1, 80000, 4000, 10.0, 80000, 80000, 10, 10, 10, 84000, 1.05),
                new EmployeeView("dep2", "male", "des1", 2, 65000, 3250, 15.5, 70000, 60000, 18, 13, 31, 68250, 1.05),
                new EmployeeView("dep1", "female", "des2", 1, 70000, 3000, 13.0, 70000, 70000, 13, 13, 13, 73000, 1.042857142857143)
        );

    }

    @Test
    void supportHaving() {
        EmployeeHaving having = EmployeeHaving.builder().avgBonusGe(4000).build();
        EmployeeQuery query = EmployeeQuery.builder().having(having).build();
        List<EmployeeView> testViews = DataAccessManager.CLIENT.aggregate(query, EmployeeView.class);

        assertThat(testViews)
                .extracting("department", "gender", "designation", "avgBonus")
                .containsExactly(
                        Tuple.tuple("dep1", "female", "des1", 4000.0),
                        Tuple.tuple("dep1", "male", "des1", 4500.0)
                );
    }

    @Test
    void supportSubquery() {
        EmployeeQuery query = EmployeeQuery.builder().gender("male").salaryGt(EmployeeQuery.builder().build()).build();
        List<EmployeeEntity> entities = DataAccessManager.query(EmployeeEntity.class, query);
        assertThat(entities)
                .extracting("id", "gender", "salary")
                .containsExactlyInAnyOrder(
                        Tuple.tuple(1, "male", 100000),
                        Tuple.tuple(2, "male", 80000)
                );
    }

}