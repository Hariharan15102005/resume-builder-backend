package in.hariharan.Resumebuilder.exception;

public class ResourceExistsException extends RuntimeException {

    public ResourceExistsException() {
        super();
    }

    public ResourceExistsException(String message) {
        super(message);
    }

    public ResourceExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
