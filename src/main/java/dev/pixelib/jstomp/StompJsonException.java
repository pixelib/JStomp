package dev.pixelib.jstomp;

/**
 * Exception thrown when JSON serialization/deserialization operations fail
 * during STOMP message processing.
 */
public class StompJsonException extends Exception {
    
    /**
     * Creates a new StompJsonException with the specified detail message.
     * 
     * @param message the detail message
     */
    public StompJsonException(String message) {
        super(message);
    }
    
    /**
     * Creates a new StompJsonException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public StompJsonException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates a new StompJsonException with the specified cause.
     * 
     * @param cause the cause of this exception
     */
    public StompJsonException(Throwable cause) {
        super(cause);
    }
}