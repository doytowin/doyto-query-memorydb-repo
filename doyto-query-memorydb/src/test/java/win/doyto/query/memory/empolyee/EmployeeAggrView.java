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

package win.doyto.query.memory.empolyee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import win.doyto.query.annotation.Column;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.annotation.View;

/**
 * EmployeeAggrView
 *
 * @author f0rb on 2024/7/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@View(EmployeeEntity.class)
public class EmployeeAggrView {
    @GroupBy
    private String department;
    @GroupBy
    private String gender;
    @GroupBy
    private String designation;

    private Integer count;
    private double avgSalary;
    private double avgBonus;
    private double avgPerks;
    private double maxSalary;
    private double minSalary;
    private Integer firstPerks;
    private Integer lastPerks;
    private Integer sumPerks;
    @Column(name = "avg(salary + bonus)")
    private double avgIncome;
    @Column(name = "sum(salary + bonus) / sum(salary)")
    private double bonusRate;
}
