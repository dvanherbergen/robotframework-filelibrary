package io.github.dvanherbergen.filelibrary;

import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;

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

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.getMessage());
        addErrorDetail(builder, getCause());
        return builder.toString();
    }

    private void addErrorDetail(StringBuilder builder, Throwable t) {
        if (t == null) {
            return;
        }
        builder.append("\n").append(t.getMessage());

        if (t instanceof SQLException) {
            SQLException nextException = null;
            nextException = ((SQLException) t).getNextException();
            while (nextException != null) {
                builder.append("\n");
                builder.append(StringUtils.trim(nextException.getMessage()));
                nextException = nextException.getNextException();
            }
        }
        addErrorDetail(builder, t.getCause());
    }
}
