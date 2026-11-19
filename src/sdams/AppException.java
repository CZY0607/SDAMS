package sdams;

/** Error with a message that is safe to show the user (bad input, wrong password, duplicate ID ...). */
public class AppException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AppException(String message) {
        super(message);
    }
}
