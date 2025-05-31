package dev.pixelib.jstomp;

/**
 * Represents a STOMP subscription.
 */
public class StompSubscription {
    
    private final String id;
    private final String destination;
    private final StompMessageHandler messageHandler;
    
    /**
     * Creates a new STOMP subscription.
     * 
     * @param id the subscription ID
     * @param destination the destination
     * @param messageHandler the message handler
     */
    public StompSubscription(String id, String destination, StompMessageHandler messageHandler) {
        this.id = id;
        this.destination = destination;
        this.messageHandler = messageHandler;
    }
    
    /**
     * Gets the subscription ID.
     * 
     * @return the ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the destination.
     * 
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }
    
    /**
     * Gets the message handler.
     * 
     * @return the message handler
     */
    public StompMessageHandler getMessageHandler() {
        return messageHandler;
    }
    
    @Override
    public String toString() {
        return "StompSubscription{" +
                "id='" + id + '\'' +
                ", destination='" + destination + '\'' +
                '}';
    }
}
