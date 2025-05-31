package dev.pixelib.jstomp;

/**
 * STOMP command enumeration.
 */
public enum StompCommand {
    // Client commands
    CONNECT,
    STOMP,
    SEND,
    SUBSCRIBE,
    UNSUBSCRIBE,
    DISCONNECT,
    ACK,
    NACK,
    BEGIN,
    COMMIT,
    ABORT,
    
    // Server commands
    CONNECTED,
    MESSAGE,
    RECEIPT,
    ERROR
}
