<p align="center">
<img src="https://github.com/user-attachments/assets/a99fb914-ec19-46c0-a530-701da168578d" />
</p>
# JStomp

A Java STOMP (Simple Text Oriented Messaging Protocol) client library built on top of OkHTTP WebSocket. This library provides a simple and efficient way to connect to STOMP-enabled message brokers like ActiveMQ, RabbitMQ, and others.

## Features

- Built on top of OkHTTP for robust WebSocket communication
- Support for STOMP 1.2 protocol
- Asynchronous connection handling with CompletableFuture
- Message subscription and publishing
- JSON serialization/deserialization with GSON support
- Connection state management
- Proper header escaping according to STOMP specification
- Comprehensive test coverage

## Requirements

- Java 21 or higher
- Gradle 8.5 or higher

## Usage

### Basic Connection and Messaging

```java
import dev.pixelib.jstomp.*;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

// Create a STOMP client
StompClient client = new StompClient(URI.create("ws://localhost:61614/stomp"));

// Set up connection listener (optional)
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

// Connect to the server
CompletableFuture<Void> connectionFuture = client.connect();
connectionFuture.join(); // Wait for connection

// Subscribe to a destination
String subscriptionId = client.subscribe("/topic/messages", message -> {
    System.out.println("Received: " + message.getBody());
    System.out.println("From: " + message.getDestination());
});

// Send a message
client.send("/topic/messages", "Hello, STOMP!");

// Unsubscribe
client.unsubscribe(subscriptionId);

// Disconnect
client.disconnect();
```

### Custom Headers

```java
// Add custom headers for authentication
client.addHeader("login", "username");
client.addHeader("passcode", "password");

// Send message with custom headers
Map<String, String> headers = Map.of(
    "content-type", "application/json",
    "priority", "high"
);
client.send("/topic/messages", "{\"message\": \"Hello JSON\"}", headers);
```

### Using Custom OkHTTP Client

```java
OkHttpClient customHttpClient = new OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build();

StompClient client = new StompClient(customHttpClient, URI.create("ws://localhost:61614/stomp"));
```

### JSON Messaging with GSON

JStomp includes built-in support for JSON serialization and deserialization using GSON:

```java
import dev.pixelib.jstomp.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;

// Example data class
public class Message {
    private String text;
    private String sender;
    private long timestamp;
    
    // constructors, getters, setters...
}

// Create client with default GSON
StompClient client = new StompClient(URI.create("ws://localhost:61614/stomp"));

// Or with custom GSON configuration
Gson customGson = new GsonBuilder()
    .setDateFormat("yyyy-MM-dd HH:mm:ss")
    .create();
StompClient clientWithCustomGson = new StompClient(
    new OkHttpClient(), 
    URI.create("ws://localhost:61614/stomp"), 
    customGson
);

client.connect().join();

// Subscribe to JSON messages
String subscriptionId = client.subscribeJson("/topic/messages", Message.class, jsonMessage -> {
    Message msg = jsonMessage.getData();
    System.out.println("Received message: " + msg.getText());
    System.out.println("From: " + msg.getSender());
    System.out.println("Timestamp: " + msg.getTimestamp());
    
    // Access original STOMP headers
    Map<String, String> headers = jsonMessage.getHeaders();
    System.out.println("Message ID: " + headers.get("message-id"));
});

// Send JSON message
Message message = new Message("Hello JSON!", "user123", System.currentTimeMillis());
client.sendJson("/topic/messages", message);

// Send JSON with custom headers
Map<String, String> headers = Map.of(
    "priority", "high",
    "content-type", "application/json"
);
client.sendJson("/topic/messages", message, headers);

// Subscribe to generic types (e.g., List<Message>)
Type listType = new TypeToken<List<Message>>(){}.getType();
client.subscribeJson("/topic/bulk-messages", listType, jsonMessage -> {
    List<Message> messages = jsonMessage.getData();
    System.out.println("Received " + messages.size() + " messages");
});

client.unsubscribe(subscriptionId);
client.disconnect();
```

### JSON Error Handling

```java
// JSON operations can throw StompJsonException for serialization/deserialization errors
try {
    client.subscribeJson("/topic/messages", Message.class, jsonMessage -> {
        // Handle message
    });
} catch (StompJsonException e) {
    System.err.println("JSON error: " + e.getMessage());
    System.err.println("Original message: " + e.getOriginalMessage());
    System.err.println("Target type: " + e.getTargetType());
}
```

## Building

To build the project:

```bash
./gradlew build
```

To run tests:

```bash
./gradlew test
```

To generate Javadocs:

```bash
./gradlew javadoc
```

## Dependencies

- **OkHTTP 4.12.0** - For WebSocket communication
- **SLF4J 2.0.9** - For logging
- **GSON 2.10.1** - For JSON serialization/deserialization
- **JUnit 5** - For testing
- **Mockito** - For mocking in tests
- **AssertJ** - For fluent assertions in tests

## STOMP Protocol Support

This library supports the STOMP 1.2 specification with the following commands:

### Client Commands
- `CONNECT` - Connect to server
- `SEND` - Send message to destination  
- `SUBSCRIBE` - Subscribe to destination
- `UNSUBSCRIBE` - Unsubscribe from destination
- `DISCONNECT` - Disconnect from server
- `ACK` - Acknowledge message
- `NACK` - Negative acknowledge message
- `BEGIN` - Begin transaction
- `COMMIT` - Commit transaction
- `ABORT` - Abort transaction

### Server Commands
- `CONNECTED` - Connection acknowledgment
- `MESSAGE` - Message from subscription
- `RECEIPT` - Receipt acknowledgment
- `ERROR` - Error from server

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for your changes
5. Ensure all tests pass
6. Submit a pull request

## Examples

See the `src/test/java/dev/pixelib/jstomp/example/` directory for more usage examples.
