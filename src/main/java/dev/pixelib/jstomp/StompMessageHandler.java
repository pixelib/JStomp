package dev.pixelib.jstomp;

/**
 * Interface for handling STOMP messages.
 */
@FunctionalInterface
public interface StompMessageHandler {
    
    /**
     * Called when a STOMP message is received.
     * 
     * @param message the received message
     */
    void onMessage(StompMessage message);
}
