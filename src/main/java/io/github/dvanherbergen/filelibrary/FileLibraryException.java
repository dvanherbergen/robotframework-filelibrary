package io.github.dvanherbergen.filelibrary;

/**
 * DataLibraryException signals that an error has occurred in a DataLibrary
 * keyword execution.
 */
public class FileLibraryException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public static final boolean ROBOT_SUPPRESS_NAME = true;

    public FileLibraryException(String message) {
        super(message);
    }

    public FileLibraryException(Throwable t) {
        super(t);
    }

    public FileLibraryException(String message, Throwable t) {
        super(message, t);
    }
}
