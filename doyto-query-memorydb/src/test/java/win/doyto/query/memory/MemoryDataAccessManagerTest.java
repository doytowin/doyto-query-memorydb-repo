package win.doyto.query.memory;

import org.junit.jupiter.api.Test;
import win.doyto.query.memory.datawrapper.FileIOException;
import win.doyto.query.memory.empolyee.EmployeeEntity;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * MemoryDataAccessManagerTest
 *
 * @author f0rb on 2024/8/12
 */
class MemoryDataAccessManagerTest {

    @Test
    void shouldFailWhenEntityDataDirNotExist() {
        assertThrows(FileIOException.class, () ->
                MemoryDataAccessManager.create(EmployeeEntity.class, "/none/exist/path"));
    }

}