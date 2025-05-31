package dev.pixelib.jstomp;

/**
 * Interface for handling STOMP connection events.
 */
public interface StompConnectionListener {
    
    /**
     * Called when the STOMP connection is established.
     */
    void onConnected();
    
    /**
     * Called when the STOMP connection is closed.
     */
    void onDisconnected();
    
    /**
     * Called when an error occurs.
     * 
     * @param error the error that occurred
     */
    void onError(Throwable error);
}
