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
