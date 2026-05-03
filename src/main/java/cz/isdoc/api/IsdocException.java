package cz.isdoc.api;

/**
 * Wraps low-level JAXB / IO failures that occur while reading, writing, or
 * packaging ISDOC documents so library consumers don't need to import the
 * underlying exception types.
 */
public class IsdocException extends RuntimeException {

    public IsdocException(String message) {
        super(message);
    }

    public IsdocException(String message, Throwable cause) {
        super(message, cause);
    }
}
