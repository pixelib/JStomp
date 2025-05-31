package dev.pixelib.jstomp;

import java.util.Map;

/**
 * Represents a STOMP message received from a subscription.
 */
public class StompMessage {
    
    private final String destination;
    private final String body;
    private final Map<String, String> headers;
    
    /**
     * Creates a new STOMP message.
     * 
     * @param destination the destination this message was received from
     * @param body the message body
     * @param headers the message headers
     */
    public StompMessage(String destination, String body, Map<String, String> headers) {
        this.destination = destination;
        this.body = body;
        this.headers = headers;
    }
    
    /**
     * Gets the destination this message was received from.
     * 
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }
    
    /**
     * Gets the message body.
     * 
     * @return the body
     */
    public String getBody() {
        return body;
    }
    
    /**
     * Gets all message headers.
     * 
     * @return the headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    /**
     * Gets a specific header value.
     * 
     * @param name the header name
     * @return the header value, or null if not found
     */
    public String getHeader(String name) {
        return headers.get(name);
    }
    
    @Override
    public String toString() {
        return "StompMessage{" +
                "destination='" + destination + '\'' +
                ", body='" + body + '\'' +
                ", headers=" + headers +
                '}';
    }
}
