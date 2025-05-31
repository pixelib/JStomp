package dev.pixelib.jstomp.example;

import dev.pixelib.jstomp.StompClient;
import dev.pixelib.jstomp.StompConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Example usage of the STOMP client.
 * 
 * This example demonstrates how to connect to a STOMP server,
 * subscribe to a destination, and send messages.
 * 
 * Note: This is placed in the test directory so it's not packaged
 * with the library but serves as documentation and integration testing.
 */
public class StompClientExample {
    
    private static final Logger logger = LoggerFactory.getLogger(StompClientExample.class);
    
    public static void main(String[] args) throws Exception {
        // Create a STOMP client
        URI serverUri = URI.create("ws://localhost:61614/stomp");
        StompClient client = new StompClient(serverUri);
        
        // Set up connection listener
        CountDownLatch connectLatch = new CountDownLatch(1);
        client.setConnectionListener(new StompConnectionListener() {
            @Override
            public void onConnected() {
                logger.info("Connected to STOMP server");
                connectLatch.countDown();
            }
            
            @Override
            public void onDisconnected() {
                logger.info("Disconnected from STOMP server");
            }
            
            @Override
            public void onError(Throwable error) {
                logger.error("STOMP error occurred", error);
            }
        });
        
        try {
            // Connect to the server
            logger.info("Connecting to STOMP server at {}", serverUri);
            client.connect().get(10, TimeUnit.SECONDS);
            
            // Wait for connection to be established
            if (!connectLatch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("Failed to connect within timeout");
            }
            
            // Subscribe to a destination
            String subscriptionId = client.subscribe("/queue/example", message -> {
                logger.info("Received message from {}: {}", 
                           message.getDestination(), message.getBody());
            });
            
            logger.info("Subscribed to /queue/example with ID: {}", subscriptionId);
            
            // Send some messages
            for (int i = 1; i <= 5; i++) {
                String messageBody = "Hello, STOMP! Message #" + i;
                client.send("/queue/example", messageBody);
                logger.info("Sent message: {}", messageBody);
                
                // Wait a bit between messages
                Thread.sleep(1000);
            }
            
            // Wait a bit to receive messages
            Thread.sleep(2000);
            
            // Unsubscribe
            client.unsubscribe(subscriptionId);
            logger.info("Unsubscribed from /queue/example");
            
        } finally {
            // Disconnect
            client.disconnect();
            logger.info("Disconnected from STOMP server");
        }
    }
}
