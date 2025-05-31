package dev.pixelib.jstomp;

/**
 * Exception thrown when STOMP operations fail.
 */
public class StompException extends RuntimeException {
    
    /**
     * Creates a new STOMP exception with the specified message.
     * 
     * @param message the error message
     */
    public StompException(String message) {
        super(message);
    }
    
    /**
     * Creates a new STOMP exception with the specified message and cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public StompException(String message, Throwable cause) {
        super(message, cause);
    }
}
