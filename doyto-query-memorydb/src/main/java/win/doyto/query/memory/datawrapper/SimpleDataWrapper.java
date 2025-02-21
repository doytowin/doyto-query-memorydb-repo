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

package win.doyto.query.memory.datawrapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SimpleDataWrapper
 *
 * @author f0rb on 2024/7/19
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleDataWrapper<E> implements DataWrapper<E> {

    static final DataWrapper<?> EMPTY = new SimpleDataWrapper<>(null);

    protected E data;

    @Override
    public E get() {
        return data;
    }

    @Override
    public void delete() {
        //nothing to delete
    }
}
