package win.doyto.query.memory.datawrapper;

/**
 * FileIOException
 *
 * @author f0rb on 2024/8/11
 */
public class FileIOException extends RuntimeException{
    public FileIOException(String message) {
        super(message);
    }

    public FileIOException(String message, Exception e) {
        super(message, e);
    }
}
