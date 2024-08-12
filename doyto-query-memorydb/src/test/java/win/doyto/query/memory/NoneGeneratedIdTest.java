package win.doyto.query.memory;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import win.doyto.query.annotation.Id;
import win.doyto.query.core.PageQuery;
import win.doyto.query.entity.Persistable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NoneGeneratedIdTest
 *
 * @author f0rb on 2024/7/17
 */
class NoneGeneratedIdTest {

    @Test
    void createNoneGeneratedIdEntity() {

        MemoryDataAccess<NoneGeneratedIdEntity, Integer, PageQuery> testMemoryDataAccess
                = MemoryDataAccessManager.create(NoneGeneratedIdEntity.class);

        NoneGeneratedIdEntity noneGeneratedIdEntity = new NoneGeneratedIdEntity();
        noneGeneratedIdEntity.setId(1);
        noneGeneratedIdEntity.setTest("some");
        testMemoryDataAccess.create(noneGeneratedIdEntity);

        assertThat(testMemoryDataAccess.count(new PageQuery())).isEqualTo(1);
    }

    @Getter
    @Setter
    public static class NoneGeneratedIdEntity implements Persistable<Integer> {
        @Id
        protected Integer id;
        private String test;
    }
}
