/*
 * Copyright © 2022-2025 DoytoWin, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.memory;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.PageQuery;
import win.doyto.query.memory.datawrapper.FileType;
import win.doyto.query.memory.empolyee.EmployeeAggrView;
import win.doyto.query.memory.empolyee.EmployeeEntity;
import win.doyto.query.memory.empolyee.EmployeeHaving;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MemoryAggregateTest
 *
 * @author f0rb on 2024/7/22
 */
class MemoryAggregateTest {

    /**
     * The data comes from <a href="https://stackoverflow.com/questions/72038822/aggregate-multiple-fields-grouping-by-multiple-fields-in-java-8">here</a>.
     */
    @BeforeAll
    static void beforeAll() {
        String path = MemoryDataAccessManager.class.getResource(File.separator).getPath();
        MemoryDataAccessManager.create(EmployeeEntity.class, path, FileType.JSON);
    }

    @Test
    void aggregate() {
        PageQuery pageQuery = PageQuery.builder().sort("maxSalary,desc;avgSalary").build();
        List<EmployeeAggrView> testViews = MemoryDataAccessManager.aggregate(EmployeeAggrView.class, pageQuery);

        assertThat(testViews).hasSize(4).containsExactly(
                new EmployeeAggrView("dep1", "male", "des1", 2, 90000, 4500, 15.0, 100000, 80000, 20, 10, 30, 94500, 1.05),
                new EmployeeAggrView("dep1", "female", "des1", 1, 80000, 4000, 10.0, 80000, 80000, 10, 10, 10, 84000, 1.05),
                new EmployeeAggrView("dep2", "male", "des1", 2, 65000, 3250, 15.5, 70000, 60000, 18, 13, 31, 68250, 1.05),
                new EmployeeAggrView("dep1", "female", "des2", 1, 70000, 3000, 13.0, 70000, 70000, 13, 13, 13, 73000, 1.042857142857143)
        );

    }

    @Test
    void supportHaving() {
        EmployeeHaving having = EmployeeHaving.builder().avgBonusGe(4000).build();
        List<EmployeeAggrView> testViews = MemoryDataAccessManager.aggregate(EmployeeAggrView.class, having);

        assertThat(testViews)
                .extracting("department", "gender", "designation", "avgBonus")
                .containsExactly(
                        Tuple.tuple("dep1", "female", "des1", 4000.0),
                        Tuple.tuple("dep1", "male", "des1", 4500.0)
                );
    }

}
