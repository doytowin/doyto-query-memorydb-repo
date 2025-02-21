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

package win.doyto.query.memory.condition;

import java.util.Collection;

/**
 * Matcher
 *
 * @author f0rb on 2021-12-10
 */
interface Matcher {

    /**
     * Filter entity objects by comparing qv and ev
     *
     * @param efv The field value of the entity object
     * @param qfv The field value of the query object
     * @return true The field value matches the filter criteria
     */
    boolean doMatch(Object efv, Object qfv);

    default boolean match(Object efv, Object qfv) {
        return isComparable(efv, qfv) && doMatch(efv, qfv);
    }

    default boolean isComparable(Object efv, Object qfv) {
        return qfv instanceof Collection || (qfv instanceof Comparable && efv instanceof Comparable);
    }
}
