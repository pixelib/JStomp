package dev.pixelib.jstomp.example;

import dev.pixelib.jstomp.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Example demonstrating JSON messaging with JStomp.
 * 
 * This example shows how to:
 * - Send and receive JSON objects
 * - Use custom GSON configuration
 * - Handle generic types like List<Message>
 * - Handle JSON errors
 */
public class JsonExample {
    
    // Example data class
    public static class Message {
        private String text;
        private String sender;
        private long timestamp;
        
        public Message() {}
        
        public Message(String text, String sender, long timestamp) {
            this.text = text;
            this.sender = sender;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        @Override
        public String toString() {
            return String.format("Message{text='%s', sender='%s', timestamp=%d}", text, sender, timestamp);
        }
    }
    
    public static void main(String[] args) throws Exception {
        // Create client with default GSON
        StompClient client = new StompClient(URI.create("ws://localhost:61614/stomp"));
        
        // Or create client with custom GSON configuration
        Gson customGson = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
        
        StompClient clientWithCustomGson = new StompClient(
            new OkHttpClient(), 
            URI.create("ws://localhost:61614/stomp"), 
            customGson
        );
        
        // Set up connection listener
        client.setConnectionListener(new StompConnectionListener() {
            @Override
            public void onConnected() {
                System.out.println("Connected to STOMP server");
            }
            
            @Override
            public void onDisconnected() {
                System.out.println("Disconnected from STOMP server");
            }
            
            @Override
            public void onError(Throwable error) {
                System.err.println("STOMP error: " + error.getMessage());
            }
        });
        
        try {
            // Connect to the server
            CompletableFuture<Void> connectionFuture = client.connect();
            connectionFuture.join(); // Wait for connection
            
            // Subscribe to JSON messages
            String subscriptionId = client.subscribeJson("/topic/messages", Message.class, (destination, object, jsonMessage) -> {
                Message msg = jsonMessage.getJsonObject();
                System.out.println("Received message: " + msg);
                
                // Access original STOMP headers
                Map<String, String> headers = jsonMessage.getHeaders();
                System.out.println("Message ID: " + headers.get("message-id"));
                System.out.println("Destination: " + jsonMessage.getDestination());
            });
            
            // Send JSON message
            Message message = new Message("Hello JSON!", "user123", System.currentTimeMillis());
            client.sendJson("/topic/messages", message);
            
            // Send JSON with custom headers
            Map<String, String> headers = Map.of(
                "priority", "high",
                "content-type", "application/json",
                "correlation-id", "msg-001"
            );
            client.sendJson("/topic/messages", message, headers);
            
            // Subscribe to generic types (e.g., List<Message>)
            Type listType = new TypeToken<List<Message>>(){}.getType();
            String bulkSubscriptionId = client.subscribeJson("/topic/bulk-messages", listType, (destination, object, jsonMessage) -> {
                @SuppressWarnings("unchecked")
                List<Message> messages = (List<Message>) jsonMessage.getJsonObject();
                System.out.println("Received " + messages.size() + " messages:");
                messages.forEach(System.out::println);
            });
            
            // Send a list of messages
            List<Message> messageList = List.of(
                new Message("First message", "user1", System.currentTimeMillis()),
                new Message("Second message", "user2", System.currentTimeMillis() + 1000),
                new Message("Third message", "user3", System.currentTimeMillis() + 2000)
            );
            client.sendJson("/topic/bulk-messages", messageList);
            
            // Wait a bit for messages to be processed
            Thread.sleep(1000);
            
            // Unsubscribe
            client.unsubscribe(subscriptionId);
            client.unsubscribe(bulkSubscriptionId);
            
        } catch (StompJsonException e) {
            System.err.println("JSON error: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getMessage());
            }
        } finally {
            // Disconnect
            client.disconnect();
        }
    }
}
