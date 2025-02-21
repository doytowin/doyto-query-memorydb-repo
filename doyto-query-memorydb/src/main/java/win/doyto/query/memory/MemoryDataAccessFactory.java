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

import org.springframework.beans.factory.BeanFactory;
import win.doyto.query.annotation.EntityType;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.service.DataAccessFactory;

import java.io.File;
import java.io.Serializable;

/**
 * MemoryDataAccessFactory
 *
 * @author f0rb on 2024/8/24
 */
public class MemoryDataAccessFactory implements DataAccessFactory {
    @Override
    public EntityType getEntityType() {
        return EntityType.MEMORY;
    }

    @Override
    public <E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery> DataAccess<E, I, Q>
    createDataAccess(BeanFactory beanFactory, Class<E> entityClass) {
        String path = this.getClass().getResource(File.separator).getPath();
        return MemoryDataAccessManager.create(entityClass, path);
    }
}
