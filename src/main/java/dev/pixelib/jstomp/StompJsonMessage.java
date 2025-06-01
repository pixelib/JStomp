package dev.pixelib.jstomp;

import java.util.Map;

/**
 * A specialized STOMP message that wraps both the original STOMP message
 * and the deserialized JSON object for convenience.
 * 
 * @param <T> the type of the deserialized JSON object
 */
public class StompJsonMessage<T> {
    
    private final StompMessage originalMessage;
    private final T jsonObject;
    
    /**
     * Creates a new StompJsonMessage.
     * 
     * @param originalMessage the original STOMP message
     * @param jsonObject the deserialized JSON object
     */
    public StompJsonMessage(StompMessage originalMessage, T jsonObject) {
        this.originalMessage = originalMessage;
        this.jsonObject = jsonObject;
    }
    
    /**
     * Gets the original STOMP message.
     * 
     * @return the original STOMP message
     */
    public StompMessage getOriginalMessage() {
        return originalMessage;
    }
    
    /**
     * Gets the deserialized JSON object.
     * 
     * @return the deserialized JSON object
     */
    public T getJsonObject() {
        return jsonObject;
    }
    
    /**
     * Gets the destination this message was received from.
     * 
     * @return the destination
     */
    public String getDestination() {
        return originalMessage.getDestination();
    }
    
    /**
     * Gets the original message body (JSON string).
     * 
     * @return the JSON string body
     */
    public String getBody() {
        return originalMessage.getBody();
    }
    
    /**
     * Gets all message headers.
     * 
     * @return the headers
     */
    public Map<String, String> getHeaders() {
        return originalMessage.getHeaders();
    }
    
    /**
     * Gets a specific header value.
     * 
     * @param name the header name
     * @return the header value, or null if not found
     */
    public String getHeader(String name) {
        return originalMessage.getHeader(name);
    }
    
    @Override
    public String toString() {
        return "StompJsonMessage{" +
                "destination='" + getDestination() + '\'' +
                ", jsonObject=" + jsonObject +
                ", originalMessage=" + originalMessage +
                '}';
    }
}