package dev.pixelib.jstomp;

/**
 * Interface for handling STOMP JSON messages.
 * This is a specialized version of StompMessageHandler that provides automatic
 * JSON deserialization of message bodies.
 * 
 * @param <T> the type of object that the JSON message body should be deserialized to
 */
@FunctionalInterface
public interface StompJsonMessageHandler<T> {
    
    /**
     * Called when a STOMP JSON message is received.
     * The message body will be automatically deserialized to the specified type.
     * 
     * @param destination the destination where the message was received
     * @param object the deserialized message body
     * @param message the original STOMP message wrapped with JSON convenience methods
     * @throws StompJsonException if JSON deserialization fails
     */
    void onJsonMessage(String destination, T object, StompJsonMessage<T> message) throws StompJsonException;
}